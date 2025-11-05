package com.caresync.ai.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务方案列表查询请求参数
 */
@Data
public class SchemeQueryDTO {
    private Long childId; // 儿童ID
    private Long workerId; // 社工ID
    private String schemeStatus; // 方案状态：DRAFT, IN_PROGRESS, COMPLETED
    private String name; // 方案名称/儿童名称（模糊查询）
    private LocalDateTime startDate; // 创建开始日期
    private LocalDateTime endDate; // 创建结束日期
    private Integer page; // 页码
    private Integer pageSize; // 每页条数
}