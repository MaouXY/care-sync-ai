package com.caresync.ai.model.DTO;

import com.caresync.ai.model.ai.ChatMessage;
import lombok.Data;

import java.util.List;

/**
 * 发送训练消息请求参数
 */
@Data
public class SendTrainingMessageDTO {
    private Long sessionId; // 训练会话ID
    private String prompt; // 用户当前的问题
    private List<ChatMessage> history; // 历史对话列表
}

