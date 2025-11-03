package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 发送训练消息请求参数
 */
@Data
public class SendTrainingMessageDTO {
    private Long sessionId; // 训练会话ID
    private String content; // 聊天内容
    private String contentType; // 内容类型（目前只有TEXT）
}

