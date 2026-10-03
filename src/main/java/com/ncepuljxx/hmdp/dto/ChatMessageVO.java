package com.ncepuljxx.hmdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 聊天消息展示对象
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    /**
     * 角色: user-用户 ai-客服
     */
    private String role;
    /**
     * 消息内容
     */
    private String content;
}
