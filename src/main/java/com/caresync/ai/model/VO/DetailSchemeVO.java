package com.caresync.ai.model.VO;

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
    private String workerName;
    
    // 儿童信息
    private ChildDetailInfo childInfo;
    
    // 方案目标列表
    private List<String> targetSuggest;
    
    // 方案进度（按周划分）
    private List<WeeklyMeasure> measuresSuggest;
    
    /**
     * 儿童详细信息内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
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
    public static class WeeklyMeasure implements Serializable {
        private String week;// 周数
        private List<TaskDetail> details;
    }
    
    /**
     * 任务详情内部类
     */
    @Data
    public static class TaskDetail implements Serializable {
        private String content;
        private String status;
        private Long assistTrackLogId;
    }
}