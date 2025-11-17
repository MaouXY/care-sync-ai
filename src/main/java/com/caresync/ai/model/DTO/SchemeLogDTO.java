package com.caresync.ai.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务方案列表查询请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "服务方案日志列表查询请求参数")
public class SchemeLogDTO {
    @Schema(description = "ID")
    private Long Id; // ID
}