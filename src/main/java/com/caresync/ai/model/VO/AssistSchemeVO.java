package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务方案响应结果
 */
@Data
public class AssistSchemeVO implements Serializable {
    private Long id;
    private Long childId;
    private Long workerId;
    private String target;
    private List<String> measures;
    private Integer cycle;
    private String schemeStatus;
    private Object aiSuggestions;
    private String workerAdjustReason;
    private Long aiAnalysisId;
    private Object evaluationIndex;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String childName;
    private String workerName;
}