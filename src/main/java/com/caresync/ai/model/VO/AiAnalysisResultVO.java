package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI分析结果响应结果
 */
@Data
public class AiAnalysisResultVO implements Serializable {
    private Long id;
    private Long childId;
    private List<String> sessionIds;
    private Object analysisResult;
    private String triggerType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String childName;
}