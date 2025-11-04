package com.caresync.ai.model.DTO;

import lombok.Data;

import java.util.List;

/**
 * 更新服务方案请求参数
 */
@Data
public class UpdateSchemeDTO {
    private Long id; // 方案ID
    private String target; // 服务目标
    private List<String> measures; // 服务措施
    private Integer cycle; // 服务周期（天）
    private String workerAdjustReason; // 社工调整理由
}