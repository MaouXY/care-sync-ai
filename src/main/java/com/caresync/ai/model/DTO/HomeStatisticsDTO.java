package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 首页统计数据DTO
 */
@Data
public class HomeStatisticsDTO {
    // 绑定儿童数
    private Integer childCount;
    
    // 今日新增儿童数
    private Integer newChildCount;
    
    // 待处理紧急呼叫数
    private Integer pendingEmergencyCount;
    
    // 已完成服务方案数
    private Integer completedSchemeCount;
}