package com.caresync.ai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.caresync.ai.handler.JsonbTypeHandler;
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
@TableName("ai_assist_scheme")
public class AiAssistScheme extends Model<AiAssistScheme> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("child_id")
    private Long childId;

    @TableField("worker_id")
    private Long workerId;

    @TableField("target")
    private String target;

    @TableField("measures")
    private Object measures;

    @TableField("cycle")
    private Integer cycle;

    @TableField("scheme_status")
    private String schemeStatus;

    @TableField(value = "ai_suggestions", typeHandler = JsonbTypeHandler.class)
    private Object aiSuggestions;

    @TableField("worker_adjust_reason")
    private String workerAdjustReason;

    @TableField("ai_analysis_id")
    private Long aiAnalysisId;

    @TableField(value = "evaluation_index", typeHandler = JsonbTypeHandler.class)
    private Object evaluationIndex;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}