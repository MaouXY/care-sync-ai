package com.caresync.ai.model.DTO;

import com.caresync.ai.model.ai.ChatRequest;
import lombok.Data;

/**
 * 发送聊天消息请求参数
 */
@Data
public class ChatMessageDTO {
    private ChatRequest chatRequest;
    private String contentType; // 内容类型：VOICE, TEXT
    private String sessionId; // 会话ID
    private String digiSessionId; // 数字人会话ID
}