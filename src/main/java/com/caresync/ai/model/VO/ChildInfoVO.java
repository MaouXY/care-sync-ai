package com.caresync.ai.model.VO;

import com.caresync.ai.model.json.AiStructInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 儿童信息响应结果
 */
@Data
@Builder
@Schema(description = "儿童信息响应结果")
public class ChildInfoVO implements Serializable {
    @Schema(description = "儿童ID")
    private Long id;
    @Schema(description = "儿童编号")
    private String childNo;
    @Schema(description = "服务状态")
    private String serviceStatus;
    @Schema(description = "风险等级")
    private String riskLevel;
    @Schema(description = "儿童姓名")
    private String name;
    @Schema(description = "儿童年龄")
    private Integer age;
    @Schema(description = "儿童性别")
    private String gender;
    @Schema(description = "儿童出生日期")
    private LocalDateTime birthDate;
    @Schema(description = "儿童身份证号")
    private String idCard;
    @Schema(description = "儿童地址")
    private String address;
    @Schema(description = "儿童备注")
    private String notes;
    @Schema(description = "儿童手机号")
    private String phone;
    @Schema(description = "监护人姓名")
    private String guardianName;
    @Schema(description = "监护人手机号")
    private String guardianPhone;
    @Schema(description = "是否有新聊天")
    private Boolean hasNewChat;

    @Schema(description = "AI分析结果")
    private AiStructInfo aiStructInfo;

    @Schema(description = "AI分析时间")
    private LocalDateTime aiAnalysisTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}