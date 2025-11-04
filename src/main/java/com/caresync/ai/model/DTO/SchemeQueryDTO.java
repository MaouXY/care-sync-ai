package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 服务方案列表查询请求参数
 */
@Data
public class SchemeQueryDTO {
    private Long childId; // 儿童ID
    private Long workerId; // 社工ID
    private String schemeStatus; // 方案状态：DRAFT, IN_PROGRESS, COMPLETED
    private Integer page; // 页码
    private Integer pageSize; // 每页条数
}