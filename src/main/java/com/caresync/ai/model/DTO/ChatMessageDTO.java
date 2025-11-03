package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 发送聊天消息请求参数
 */
@Data
public class ChatMessageDTO {
    private String content; // 聊天内容
    private String contentType; // 内容类型：VOICE, TEXT
    private String sessionId; // 会话ID
    private String digiSessionId; // 数字人会话ID
}