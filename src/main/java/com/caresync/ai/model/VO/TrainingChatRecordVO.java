package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 训练聊天记录响应结果
 */
@Data
public class TrainingChatRecordVO implements Serializable {
    private Long id; // 记录ID
    private Long sessionId; // 训练会话ID
    private Integer roundNum; // 对话轮次
    private String contentType; // 内容类型
    private String content; // 聊天内容
    private Boolean isAiReply; // 是否AI回复
    private Map<String, Object> emotionAnalysis; // 情感分析结果
    private String aiGuidance; // AI指导意见
    private LocalDateTime createTime; // 创建时间
}

