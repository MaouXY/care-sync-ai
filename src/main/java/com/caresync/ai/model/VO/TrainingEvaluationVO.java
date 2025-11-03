package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 训练评估响应结果
 */
@Data
public class TrainingEvaluationVO implements Serializable {
    private Long id; // 评估ID
    private Long sessionId; // 训练会话ID
    private BigDecimal empathyScore; // 共情能力评分
    private BigDecimal communicationScore; // 沟通技巧评分
    private BigDecimal problemSolvingScore; // 问题解决能力评分
    private BigDecimal emotionalRecognitionScore; // 情感识别能力评分
    private String strengths; // 优势
    private String areasForImprovement; // 改进点
    private String aiComprehensiveComment; // AI综合评价
    private LocalDateTime createTime; // 创建时间
}

