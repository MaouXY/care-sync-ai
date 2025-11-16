package com.caresync.ai.model.VO;

import com.caresync.ai.model.json.MeasuresSuggest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务方案日志响应结果
 */
@Data
@Schema(description = "服务方案响应结果")
public class AssistSchemeLogVO {
    @Schema(description = "服务方案ID")
    private Long id;
    @Schema(description = "儿童ID")
    private Long childId;
    @Schema(description = "服务人员ID")
    private Long workerId;
    @Schema(description = "服务目标")
    private String target;
    @Schema(description = "服务Tag")
    private List<String> measures;
    @Schema(description = "服务周期（周）")
    private Integer cycle;
    @Schema(description = "服务方案状态")
    private String schemeStatus;

    //完成进度
    @Schema(description = "完成任务数")
    private Integer progress;
    @Schema(description = "进行中任务数")
    private Integer inProgressTasks;
    @Schema(description = "总任务数")
    private Integer totalTasks;

    //服务建议
    @Schema(description = "AI服务目标建议")
    private String[] targetSuggest;
    @Schema(description = "AI服务措施建议")
    private MeasuresSuggest[] measuresSuggest;

    @Schema(description = "服务人员调整原因")
    private String workerAdjustReason;
    @Schema(description = "AI分析ID")
    private Long aiAnalysisId;
    @Schema(description = "评估指标")
    private Object evaluationIndex;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // 扩展字段
    @Schema(description = "儿童姓名")
    private String childName;
    @Schema(description = "儿童年龄")
    private String childAge;
    @Schema(description = "服务人员姓名")
    private String workerName;
}