package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 首页统计数据DTO
 */
@Data
public class HomeStatisticsDTO {
    // 服务儿童
    private Integer childCount;
    
    // 待处理任务
    private Integer pendingTaskCount;
    
    // 已完成方案
    private Integer completedSchemeCount;
    
    // AI分析结果
    private Integer aiAnalysisCount;
}