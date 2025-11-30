package com.caresync.ai.model.VO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务方案列表响应结果
 * 用于列表查询，只包含必要的字段
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略null值字段
public class AssistSchemeListVO implements Serializable {
    private Long id;
    private Long childId;
    private String target; // 服务目标
    private Integer cycle; // 服务周期（天）
    private String schemeStatus; // 服务方案状态

    // 从ai_suggestions JSON中解析的字段
    @JsonProperty("target_suggest") // 指定JSON字段名为target_suggest
    private List<String> targetSuggest; // 方案目标
    
    // 扩展字段
    private String childName;
    private String childAge;
    private String workerName;
    
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}