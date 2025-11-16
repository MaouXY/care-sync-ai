package com.caresync.ai.model.VO;

import com.caresync.ai.model.json.MeasuresSuggest;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务方案响应结果
 */
@Data
public class AssistSchemeVO implements Serializable {
    private Long id;
    private Long childId;
    private Long workerId;
    private String target;//服务目标
    private Integer cycle;//服务周期（天）
    private List<String> measures;//服务tag TODO
    private String schemeStatus;//服务方案状态

    // 从ai_suggestions JSON中解析的字段
    private List<String> targetSuggest;//方案目标
    private List<Map<String, Object>> measuresSuggest;//服务措施建议，包含周次和任务详情
    private Map<String, Object> evaluationIndex;//评估指标
    
    // 原始JSON字段
    private Object aiSuggestions;//AI建议原始JSON

    private String workerAdjustReason;//工作人员调整原因
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间

    // 扩展字段
    private String childName;
    private String childAge;
    private String workerName;
}