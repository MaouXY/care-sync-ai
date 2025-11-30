package com.caresync.ai.model.VO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务方案详情响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略null值字段
public class DetailSchemeVO implements Serializable {
    // 方案基本信息
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 仅反序列化时使用，序列化时忽略
    private Long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String target;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createTime;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer cycle;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String schemeStatus;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String workerAdjustReason;
    
    // 社工信息
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long workerId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String workerName;
    
    // 儿童信息
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ChildDetailInfo childInfo;
    
    // 方案目标列表
    @JsonProperty("target_suggest") // 指定JSON字段名为target_suggest
    private List<String> targetSuggest;
    
    // 方案进度（按周划分）
    @JsonProperty("measures_suggest") // 指定JSON字段名为measures_suggest
    private List<WeeklyMeasure> measuresSuggest;
    
    /**
     * 儿童详细信息内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChildDetailInfo implements Serializable {
        private Long id;
        private String name;
        private Integer age;
        private String gender;
        private String riskLevel;
        private Map<String, Object> emotionScores; // AI结构化信息中的情绪分数
        private List<String> emotionTrend; // AI结构化信息中的情感趋势
    }
    
    /**
     * 每周措施内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeeklyMeasure implements Serializable {
        private String week;// 周数
        private List<TaskDetail> details;
    }
    
    /**
     * 任务详情内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaskDetail implements Serializable {
        private String content;
        private String status;
        @JsonProperty("assist_track_log_id") // 指定JSON字段名为assist_track_log_id
        private Long assistTrackLogId;
    }
}