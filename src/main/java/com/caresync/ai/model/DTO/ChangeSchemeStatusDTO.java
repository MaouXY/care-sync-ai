package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 修改方案状态请求参数
 */
@Data
public class ChangeSchemeStatusDTO {
    private Long id; // 方案ID
    private String schemeStatus; // 新的方案状态：DRAFT, IN_PROGRESS, COMPLETED
}