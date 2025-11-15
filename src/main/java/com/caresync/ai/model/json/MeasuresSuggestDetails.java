package com.caresync.ai.model.json;

import lombok.Data;

@Data
public class MeasuresSuggestDetails {
    private String content;//任务内容
    private String status;//任务状态 status字段支持三种状态：pending(待处理)/in_progress(进行中)/completed(已完成)
    private Long assist_track_log_id;//关联的服务记录ID
}
