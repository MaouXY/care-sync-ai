package com.caresync.ai.model.json;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI分析结果结构体")
public class AiStructInfo {
    @Schema(description = "情感趋势")
    private String[] emotionTrend;
    @Schema(description = "核心需求")
    private String[] coreNeeds;
    @Schema(description = "潜在问题")
    private String potentialProblems;
    @Schema(description = "服务描述")
    private String description;
    @Schema(description = "最新分析时间")
    private LocalDateTime latestAnalysis;
    @Schema(description = "情感评分指标")
    private EmotionScores emotionScores;
    @Schema(description = "情感历史记录")
    private EmotionScores[] emotionHistory;
    @Schema(description = "关键发现列表")
    private String[] keyFindings;
    @Schema(description = "建议列表")
    private Recommendations[] recommendations;
}
