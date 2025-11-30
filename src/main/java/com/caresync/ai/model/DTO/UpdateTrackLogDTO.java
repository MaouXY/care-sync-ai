package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 更新服务日志请求参数
 */
@Data
public class UpdateTrackLogDTO {
    private Long id; // 日志ID
    private String completionStatus; //status字段支持三种状态：pending(待处理)/in_progress(进行中)/completed(已完成)
    private String recordContent; // 记录内容
}
