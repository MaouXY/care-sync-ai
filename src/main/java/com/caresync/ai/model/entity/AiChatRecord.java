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
@TableName("ai_chat_record")
public class AiChatRecord extends Model<AiChatRecord> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("child_id")
    private Long childId;

    @TableField("session_id")
    private String sessionId;

    @TableField("digi_session_id")
    private String digiSessionId;

    @TableField("round_num")
    private Integer roundNum;

    @TableField("content_type")
    private String contentType;

    @TableField("content")
    private String content;

    @TableField("is_ai_reply")
    private Boolean aiReply;

    @TableField("is_filtered")
    private Boolean filtered;

    @TableField("emotion_tag")
    private String emotionTag;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
