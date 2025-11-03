package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI指导意见响应结果
 */
@Data
public class TrainingGuidanceVO implements Serializable {
    private Long sessionId; // 训练会话ID
    private String guidanceContent; // 指导意见内容
    private String emotionAnalysis; // 情感分析
    private String suggestions; // 建议
    private LocalDateTime createTime; // 创建时间
}
