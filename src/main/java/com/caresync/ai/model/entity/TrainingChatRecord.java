package com.caresync.ai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.caresync.ai.handler.JsonbTypeHandler;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;

/**
 * <p>
 * 
 * </p>
 *
 * @author Maou
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("training_chat_record")
public class TrainingChatRecord extends Model<TrainingChatRecord> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("round_num")
    private Integer roundNum;

    @TableField("content_type")
    private String contentType;

    @TableField("content")
    private String content;

    @TableField("is_ai_reply")
    private Boolean aiReply;

    @TableField(value = "emotion_analysis", typeHandler = JsonbTypeHandler.class)
    private String emotionAnalysis;

    @TableField("ai_guidance")
    private String aiGuidance;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}