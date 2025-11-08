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
@TableName("ai_analysis_log")
public class AiAnalysisLog extends Model<AiAnalysisLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("child_id")
    private Long childId;

    @TableField("session_ids")
    private Object sessionIds;

    @TableField(value = "analysis_result", typeHandler = JsonbTypeHandler.class)
    private Object analysisResult;

    @TableField("trigger_type")
    private String triggerType;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}