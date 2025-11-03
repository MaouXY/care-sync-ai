package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI指导意见响应结果
 */
@Data
public class AiGuidanceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 指导类型：沟通技巧、情感支持、行为引导等
     */
    private String guidanceType;

    /**
     * 指导内容
     */
    private String guidanceContent;

    /**
     * 建议改进点
     */
    private String improvementSuggestions;

    /**
     * 当前表现评分
     */
    private Integer score;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}