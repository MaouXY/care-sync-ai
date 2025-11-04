package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 添加服务日志请求参数
 */
@Data
public class AddTrackLogDTO {
    private Long schemeId; // 服务方案ID
    private Integer week; // 服务周次
    private String completionStatus; // 完成状态：COMPLETED（已完成）/UNFINISHED（未完成）
    private String recordContent; // 记录内容
}