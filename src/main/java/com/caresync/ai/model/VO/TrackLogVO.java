package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务跟踪日志响应结果
 */
@Data
public class TrackLogVO implements Serializable {
    private Long id;
    private Long schemeId;
    private Long workerId;
    private Integer week;
    private String completionStatus;
    private String recordContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String workerName;
    private String schemeTarget;
}