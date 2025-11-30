package com.caresync.ai.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 措施建议详情
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasuresSuggestDetails {
    @JsonProperty("content")
    private String content;//任务内容
    
    @JsonProperty("status")
    private String status;//任务状态 status字段支持三种状态：pending(待处理)/in_progress(进行中)/completed(已完成)
    
    @JsonProperty("assist_track_log_id")
    private Long assist_track_log_id;//关联的服务记录ID
}