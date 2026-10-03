package com.ncepuljxx.hmdp.utils;

import com.ncepuljxx.hmdp.dto.ChatMessageVO;
import com.ncepuljxx.hmdp.dto.ChatSessionSummary;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis版会话记忆存储,所有会话统一存放在Redis的memory命名空间下,永不删除(不设置过期时间):
 * 1.工作窗口键 memory:chat:{memoryId}:langchain4j每个请求按"读store→加消息→写store"周期工作,
 *   存MessageWindowChatMemory裁剪后的最近窗口(供AI上下文使用)
 * 2.归档键 memory:archive:{memoryId}:每轮对话完成后追加用户消息+AI回复,只增不改,历史永不丢失
 * 3.用户会话索引 memory:index:{userId} (zset,score=最后更新时间):每个用户的会话按用户隔离
 */
@Slf4j
@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    // 工作窗口键前缀(AI上下文,随对话滚动更新)
    public static final String MEMORY_KEY_PREFIX = "memory:chat:";
    // 完整归档键前缀(只追加,永不覆盖)
    public static final String ARCHIVE_KEY_PREFIX = "memory:archive:";
    // 用户会话索引前缀(按最后更新时间排序的zset,每个用户一个)
    public static final String MEMORY_INDEX_PREFIX = "memory:index:";
    // 记忆键的用户前缀(memoryId格式: user:{userId})
    public static final String USER_MEMORY_PREFIX = "user:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ============ langchain4j 上下文接口 ============

    /**
     * langchain4j读取会话窗口;MessageWindowChatMemory会自行裁剪到maxMessages
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = stringRedisTemplate.opsForValue().get(MEMORY_KEY_PREFIX + memoryId);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.warn("会话窗口解析失败: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    /**
     * langchain4j写回会话窗口(读-改-写周期必需,否则消息丢失且对话报错)
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 写入窗口,不设置过期时间(永不删除)
        stringRedisTemplate.opsForValue().set(MEMORY_KEY_PREFIX + memoryId,
                ChatMessageSerializer.messagesToJson(list));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // 对话永不删除
    }

    // ============ 归档 ============

    /**
     * 追加一轮对话(用户消息+AI完整回复)到归档键,只增不改
     */
    public synchronized void appendExchange(String memoryId, String userText, String aiText) {
        if (memoryId == null || userText == null || aiText == null) {
            return;
        }
        String archiveKey = ARCHIVE_KEY_PREFIX + memoryId;
        String json = stringRedisTemplate.opsForValue().get(archiveKey);
        List<ChatMessage> all;
        if (json == null || json.isBlank()) {
            all = new ArrayList<>();
        } else {
            try {
                all = ChatMessageDeserializer.messagesFromJson(json);
            } catch (Exception e) {
                log.warn("会话归档解析失败,将重建: {}", memoryId, e);
                all = new ArrayList<>();
            }
        }
        all.add(UserMessage.from(userText));
        all.add(AiMessage.from(aiText));
        // 写入完整归档,不设置过期时间(永不删除)
        stringRedisTemplate.opsForValue().set(archiveKey, ChatMessageSerializer.messagesToJson(all));
        // 更新该用户自己的会话索引,score=最后更新时间(毫秒)
        Long userId = parseUserId(memoryId);
        if (userId != null) {
            stringRedisTemplate.opsForZSet().add(MEMORY_INDEX_PREFIX + userId, memoryId, System.currentTimeMillis());
        }
    }

    // ============ 历史查询 ============

    /**
     * 列出指定用户的所有历史会话,按最后更新时间倒序
     */
    public List<ChatSessionSummary> listConversations(Long userId) {
        List<ChatSessionSummary> result = new ArrayList<>();
        String indexKey = MEMORY_INDEX_PREFIX + userId;
        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(indexKey, 0, -1);
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        for (String memoryId : ids) {
            List<ChatMessage> messages = readArchive(memoryId);
            if (messages.isEmpty()) {
                continue;
            }
            Double score = stringRedisTemplate.opsForZSet().score(indexKey, memoryId);
            ChatSessionSummary s = new ChatSessionSummary();
            s.setMemoryId(memoryId);
            s.setMessageCount(messages.size());
            s.setFirstMessage(firstUserText(messages));
            s.setLastMessage(textOf(messages.get(messages.size() - 1)));
            s.setUpdateTime(score == null ? null : LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(score.longValue()), ZoneId.systemDefault()));
            result.add(s);
        }
        return result;
    }

    /**
     * 查询指定用户某个会话的完整消息(只含用户和AI消息,供前端展示),校验会话归属防止越权
     */
    public List<ChatMessageVO> getConversationMessages(Long userId, String memoryId) {
        // 会话必须属于该用户(memoryId格式: user:{userId}:{sessionId})
        if (memoryId == null || !memoryId.startsWith(USER_MEMORY_PREFIX + userId + ":")) {
            return new ArrayList<>();
        }
        return readArchive(memoryId).stream()
                .filter(m -> m instanceof UserMessage || m instanceof AiMessage)
                .map(m -> new ChatMessageVO(
                        m instanceof UserMessage ? "user" : "ai",
                        textOf(m)))
                .collect(Collectors.toList());
    }

    /**
     * 从memoryId解析userId(memoryId格式: user:{userId}:{sessionId})
     */
    private Long parseUserId(String memoryId) {
        if (memoryId == null || !memoryId.startsWith(USER_MEMORY_PREFIX)) {
            return null;
        }
        // user:{userId}:{sessionId} -> 取中间段
        String rest = memoryId.substring(USER_MEMORY_PREFIX.length());
        int colon = rest.indexOf(':');
        String userIdStr = colon < 0 ? rest : rest.substring(0, colon);
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============ 私有方法 ============

    /**
     * 读取完整归档(只增不改)
     */
    private List<ChatMessage> readArchive(String memoryId) {
        String json = stringRedisTemplate.opsForValue().get(ARCHIVE_KEY_PREFIX + memoryId);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.warn("会话归档解析失败: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    private String firstUserText(List<ChatMessage> messages) {
        for (ChatMessage m : messages) {
            if (m instanceof UserMessage) {
                String text = textOf(m);
                return text.length() > 40 ? text.substring(0, 40) + "…" : text;
            }
        }
        return "";
    }

    /**
     * 提取消息文本:UserMessage用singleText,AiMessage用text
     */
    private String textOf(ChatMessage m) {
        if (m instanceof UserMessage) {
            return ((UserMessage) m).singleText();
        }
        if (m instanceof AiMessage) {
            return ((AiMessage) m).text() == null ? "" : ((AiMessage) m).text();
        }
        return "";
    }
}
