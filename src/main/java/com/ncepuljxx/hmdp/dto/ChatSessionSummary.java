package com.ncepuljxx.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 历史会话摘要
 * </p>
 */
@Data
public class ChatSessionSummary {
    /**
     * 会话标识
     */
    private String memoryId;
    /**
     * 第一条用户消息(截断展示)
     */
    private String firstMessage;
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    /**
     * 消息条数
     */
    private Integer messageCount;
    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
