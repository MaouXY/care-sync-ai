package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI分析结果响应结果
 */
@Data
public class AiAnalysisResultVO implements Serializable {
    private Long id;
    private Long childId;
    private List<String> sessionIds;
    private Object analysisResult;
    private String triggerType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String childName;
    private List<String> emotionTrendTags; // 情感趋势标签（对应图表X轴）
    private String potentialProblems; // 潜在问题标签（红色预警展示）
    private String description; // 分析摘要（综合描述）
    private String latestAnalysis; // 最新分析时间（页面右上角标注）
    private Map<String, Integer> emotionScores; // 情感评分指标（雷达图数据）
    private List<EmotionHistoryItem> emotionHistory; // 情感历史记录（趋势图数据）
    private List<String> keyFindings; // 关键发现列表（分析结果详情）
    private List<Recommendation> recommendations; // 建议列表（分析结果详情->服务计划生成依据）
    private Object aiStructInfo; // AI结构化数据（原始数据）
    
    /**
     * 情感历史记录项
     */
    @Data
    public static class EmotionHistoryItem implements Serializable {
        private String date;
        private Map<String, Integer> scores;
    }
    
    /**
     * 建议项
     */
    @Data
    public static class Recommendation implements Serializable {
        private String title;
        private String description;
        private String priority; // 优先级（影响展示排序）
    }
}