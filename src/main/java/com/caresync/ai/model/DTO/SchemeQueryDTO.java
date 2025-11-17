package com.caresync.ai.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务方案列表查询请求参数
 */
@Data
@Schema(description = "服务方案列表查询请求参数")
public class SchemeQueryDTO {
    @Schema(description = "儿童ID")
    private Long childId; // 儿童ID
    @Schema(description = "服务人员ID")
    private Long workerId; // 社工ID
    @Schema(description = "服务目标")
    private String target;
    @Schema(description = "服务方案状态，方案状态：DRAFT, IN_PROGRESS, COMPLETED")
    private String schemeStatus; // 方案状态：DRAFT, IN_PROGRESS, COMPLETED
    @Schema(description = "方案名称/儿童名称（模糊查询）")
    private String name; // 方案名称/儿童名称（模糊查询）
    @Schema(description = "创建开始日期")
    private LocalDateTime startDate; // 创建开始日期
    @Schema(description = "创建结束日期")
    private LocalDateTime endDate; // 创建结束日期
    @Schema(description = "页码")
    private Integer page=1; // 页码
    @Schema(description = "每页条数")
    private Integer pageSize=10; // 每页条数
}