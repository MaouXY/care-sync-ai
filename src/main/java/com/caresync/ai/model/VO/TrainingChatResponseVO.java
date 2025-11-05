package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 训练对话响应结果
 * 包含会话ID、三次回复内容和时间戳
 */
@Data
public class TrainingChatResponseVO implements Serializable {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 第一次回复内容（儿童模拟回复）
     */
    private String childReply;
    
    /**
     * 第二次回复内容（情感分析结果）
     */
    private String emotionAnalysis;
    
    /**
     * 第三次回复内容（AI指导意见）
     */
    private String aiGuidance;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}