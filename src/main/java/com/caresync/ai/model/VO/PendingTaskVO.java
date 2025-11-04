package com.caresync.ai.model.VO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 待处理任务VO（进行中的服务方案）
 */
@Data
public class PendingTaskVO {
    // 方案ID
    private Long id;
    
    // 儿童ID
    private Long childId;
    
    // 儿童姓名
    private String childName;
    
    // 服务目标
    private String target;
    
    // 结束时间
    private LocalDateTime endTime;
    
    // 剩余天数
    private Integer remainingDays;
}