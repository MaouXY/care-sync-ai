package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 更新服务日志请求参数
 */
@Data
public class UpdateTrackLogDTO {
    private Long id; // 日志ID
    private String completionStatus; // 完成状态：COMPLETED（已完成）/UNFINISHED（未完成）
    private String recordContent; // 记录内容
}