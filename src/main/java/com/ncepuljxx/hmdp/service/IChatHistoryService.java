package com.ncepuljxx.hmdp.service;

import com.ncepuljxx.hmdp.dto.ChatMessageVO;
import com.ncepuljxx.hmdp.dto.ChatSessionSummary;

import java.util.List;

/**
 * <p>
 *  聊天历史服务类
 * </p>
 *
 */
public interface IChatHistoryService {

    /**
     * 归档一轮对话(用户消息+AI回复)
     *
     * @param memoryId 会话标识
     * @param userText 用户消息
     * @param aiText   AI完整回复
     */
    void appendExchange(String memoryId, String userText, String aiText);

    /**
     * 列出指定用户的所有历史会话
     *
     * @param userId 用户id
     * @return 会话摘要列表(按更新时间倒序)
     */
    List<ChatSessionSummary> listSessions(Long userId);

    /**
     * 查询指定用户某个会话的完整消息
     *
     * @param userId   用户id
     * @param memoryId 会话标识
     * @return 消息列表
     */
    List<ChatMessageVO> getMessages(Long userId, String memoryId);
}
