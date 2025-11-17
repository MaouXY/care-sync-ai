package com.caresync.ai.model.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务统计响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "任务统计响应结果")
public class TaskStatisticsVO implements Serializable {

    @Schema(description = "完成任务数")
    private Integer progress;

    @Schema(description = "进行中任务数")
    private Integer inProgressTasks;

    @Schema(description = "总任务数")
    private Integer totalTasks;
}
