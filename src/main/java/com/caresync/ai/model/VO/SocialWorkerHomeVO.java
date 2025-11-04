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
    // 第一行数据（写死）
    private HomeStatisticsDTO statistics;
    
    // 情感趋势分析
    private Map<String, Double> emotionScoresAverage;
    
    // 儿童情况分布
    private Map<String, Integer> potentialProblemsDistribution;
    
    // 待处理任务（进行中的服务方案）
    private List<PendingTaskVO> pendingTasks;
    
    // 近期活动（最近完成的子任务）
    private List<RecentActivityVO> recentActivities;
}