package com.caresync.ai.model.VO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 近期活动VO（最近完成的子任务）
 */
@Data
public class RecentActivityVO {
    // 日志ID
    private Long id;
    
    // 方案ID
    private Long schemeId;
    
    // 儿童ID
    private Long childId;
    
    // 儿童姓名
    private String childName;
    
    // 周次
    private Integer week;
    
    // 记录内容
    private String recordContent;
    
    // 创建时间
    private LocalDateTime createTime;
}