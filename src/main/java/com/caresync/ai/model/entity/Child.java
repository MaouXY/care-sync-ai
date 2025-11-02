package com.caresync.ai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Getter
@Setter
@TableName("child")
public class Child extends Model<Child> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("child_no")
    private String childNo;

    @TableField("name")
    private String name;

    @TableField("age")
    private Integer age;

    @TableField("verify_code")
    private String verifyCode;

    @TableField("has_new_chat")
    private Boolean hasNewChat;

    @TableField("ai_struct_info")
    private Object aiStructInfo;

    @TableField("ai_analysis_time")
    private LocalDateTime aiAnalysisTime;

    @TableField("interest_tags")
    private Object interestTags;

    @TableField("study_situation")
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
