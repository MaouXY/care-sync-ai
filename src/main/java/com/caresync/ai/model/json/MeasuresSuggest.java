package com.caresync.ai.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 措施建议
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasuresSuggest {
    @JsonProperty("week")
    private String week;//每周服务tag建议
    
    @JsonProperty("details")
    private MeasuresSuggestDetails[] details;//每周任务列表
}