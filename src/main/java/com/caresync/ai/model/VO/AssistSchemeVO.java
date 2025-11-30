package com.caresync.ai.model.VO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务方案响应结果
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略null值字段
public class AssistSchemeVO implements Serializable {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 仅反序列化时使用，序列化时忽略
    private Long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long childId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long workerId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String target;//服务目标
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer cycle;//服务周期（天）
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> measures;//服务tag TODO
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String schemeStatus;//服务方案状态

    // 从ai_suggestions JSON中解析的字段
    @JsonProperty("target_suggest") // 指定JSON字段名为target_suggest
    private List<String> targetSuggest;//方案目标
    @JsonProperty("measures_suggest") // 指定JSON字段名为measures_suggest
    private List<Map<String, Object>> measuresSuggest;//服务措施建议，包含周次和任务详情
    @JsonProperty("evaluation_index") // 指定JSON字段名为evaluation_index
    private Map<String, Object> evaluationIndex;//评估指标
    
    // 原始JSON字段
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Object aiSuggestions;//AI建议原始JSON

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String workerAdjustReason;//工作人员调整原因
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createTime;//创建时间
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime updateTime;//更新时间

    // 扩展字段
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String childName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String childAge;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String workerName;
}