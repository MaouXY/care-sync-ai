package com.caresync.ai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 儿童表
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("child")
@Schema(description = "儿童实体类")
public class Child extends Model<Child> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "儿童ID")
    private Long id;

    @TableField("child_no")
    @Schema(description = "儿童编号")
    private String childNo;

    @TableField("social_worker_id")
    @Schema(description = "绑定的社会工作者ID")
    private Long socialWorkerId;

    @TableField("service_status")
    @Schema(description = "服务状态：未指定服务社工、服务中、已完成")
    private String serviceStatus;

    @TableField("risk_level")
    @Schema(description = "风险等级：低风险、中风险、高风险、紧急")
    private String riskLevel;

    @TableField("name")
    @Schema(description = "儿童姓名")
    private String name;

    @TableField("age")
    @Schema(description = "儿童年龄")
    private Integer age;

    @TableField("gender")
    @Schema(description = "性别：男/女")
    private String gender;

    @TableField("birth_date")
    @Schema(description = "出生日期")
    private LocalDate birthDate;

    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;

    @TableField("address")
    @Schema(description = "家庭地址")
    private String address;

    @TableField("notes")
    @Schema(description = "备注信息")
    private String notes;

    @TableField("phone")
    @Schema(description = "联系电话")
    private String phone;

    @TableField("guardian_name")
    @Schema(description = "监护人姓名")
    private String guardianName;

    @TableField("guardian_phone")
    @Schema(description = "监护人电话")
    private String guardianPhone;

    @TableField("verify_code")
    @Schema(description = "儿童验证码")
    private String verifyCode;

    @TableField("has_new_chat")
    @Schema(description = "是否有新的聊天记录")
    private Boolean hasNewChat;

    @TableField("ai_struct_info")
    @Schema(description = "AI结构化信息")
    private Object aiStructInfo;

    @TableField("ai_analysis_time")
    @Schema(description = "AI分析时间")
    private LocalDateTime aiAnalysisTime;

    @TableField("interest_tags")
    @Schema(description = "兴趣标签")
    private Object interestTags;

    @TableField("study_situation")
    @Schema(description = "学习情况")
    private Object studySituation;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}