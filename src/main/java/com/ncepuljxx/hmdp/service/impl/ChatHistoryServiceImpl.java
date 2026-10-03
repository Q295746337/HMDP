package com.ncepuljxx.hmdp.service.impl;

import com.ncepuljxx.hmdp.dto.ChatMessageVO;
import com.ncepuljxx.hmdp.dto.ChatSessionSummary;
import com.ncepuljxx.hmdp.service.IChatHistoryService;
import com.ncepuljxx.hmdp.utils.RedisChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class ChatHistoryServiceImpl implements IChatHistoryService {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Override
    public void appendExchange(String memoryId, String userText, String aiText) {
        redisChatMemoryStore.appendExchange(memoryId, userText, aiText);
    }

    @Override
    public List<ChatSessionSummary> listSessions(Long userId) {
        return redisChatMemoryStore.listConversations(userId);
    }

    @Override
    public List<ChatMessageVO> getMessages(Long userId, String memoryId) {
        return redisChatMemoryStore.getConversationMessages(userId, memoryId);
    }
}
