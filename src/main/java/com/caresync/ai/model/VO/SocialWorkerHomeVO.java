package com.caresync.ai.model.VO;

import com.caresync.ai.model.DTO.HomeStatisticsDTO;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 社工首页信息VO
 */
@Data
public class SocialWorkerHomeVO {
    // 第一行数据
    private HomeStatisticsDTO statistics;
    
    // 情感趋势分析
    // 儿童情感数据图表数据
    private List<Map<String, Object>> emotionChartData;

    // 情感数据图表的时间轴（横坐标）
    private List<String> emotionChartTimeAxis;
    
    // 雷达图数据（情感评分占比）
    private Map<String, Double> radarChartData;
    
    // 待处理任务（进行中的服务方案）
    private List<PendingTaskVO> pendingTasks;
    
    // 近期活动（最近完成的子任务）
    private List<RecentActivityVO> recentActivities;
}