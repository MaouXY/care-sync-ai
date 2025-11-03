package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 训练会话响应结果
 */
@Data
public class TrainingSessionVO implements Serializable {
    private Long id; // 会话ID
    private Long workerId; // 社工ID
    private Long scenarioId; // 场景ID
    private String sessionStatus; // 会话状态
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime; // 结束时间
    private Integer totalRounds; // 总对话轮次
    private BigDecimal overallScore; // 总体评分
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}