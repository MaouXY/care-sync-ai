package com.caresync.ai.model.Json;

import lombok.Data;

import java.util.List;

/**
 * 评估指标（对应ai_assist_scheme表的evaluation_index字段）
 */
@Data
public class EvaluationIndex {
    private List<KeyIndicator> keyIndicators; // 关键指标
    private List<Milestone> milestones; // 里程碑

    @Data
    public static class KeyIndicator {
        private String name; // 指标名称
        private Integer targetValue; // 目标值
        private Integer currentValue; // 当前值
        private String trend; // 趋势
    }

    @Data
    public static class Milestone {
        private Integer week; // 周次
        private String description; // 描述
        private String status; // 状态
    }
}

