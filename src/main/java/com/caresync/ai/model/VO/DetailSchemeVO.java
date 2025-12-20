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
    private Long id;
    private String target;
    private LocalDateTime createTime;
    private Integer cycle;
    private String schemeStatus;
    private String workerAdjustReason;
    
    // 社工信息
    private Long workerId;
    private WorkerDetailInfo workerInfo;
    
    // 儿童信息
    private ChildDetailInfo childInfo;
    
    // 方案目标列表
    @JsonProperty("target_suggest") // 指定JSON字段名为target_suggest
    private List<String> targetSuggest;
    
    // 方案进度（按周划分）
    @JsonProperty("measures_suggest") // 指定JSON字段名为measures_suggest
    private List<WeeklyMeasure> measuresSuggest;
    
    /**
     * 社工详细信息内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkerDetailInfo implements Serializable {
        private Long id;
        private String workerNo;
        private String workerName;
        private String name;
        private String phone;
        private String role;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
    
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
        private String childNo;
        private String name;
        private Integer age;
        private String gender;
        private String riskLevel;
        private String serviceStatus;
        private String address;
        private String phone;
        private String guardianName;
        private String guardianPhone;
        private Boolean hasNewChat;
        private LocalDateTime aiAnalysisTime;
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