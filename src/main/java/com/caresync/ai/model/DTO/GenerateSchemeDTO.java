package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 生成服务方案请求参数
 */
@Data
public class GenerateSchemeDTO {
    private Long childId; // 儿童ID
    private String additionalInfo; // 目标
    private String[] measures; // 措施
}