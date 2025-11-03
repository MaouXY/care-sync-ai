package com.caresync.ai.model.DTO;

import lombok.Data;

import java.util.List;

/**
 * 更新帮扶方案请求参数
 */
@Data
public class UpdateSchemeDTO {
    private Long id; // 方案ID
    private String target; // 帮扶目标
    private List<String> measures; // 帮扶措施
    private Integer cycle; // 帮扶周期（天）
    private String workerAdjustReason; // 社工调整理由
}