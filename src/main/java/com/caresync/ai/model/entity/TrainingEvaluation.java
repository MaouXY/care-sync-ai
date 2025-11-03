package com.caresync.ai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Maou
 * @since 2025-11-04
 */
@Getter
@Setter
@TableName("training_evaluation")
public class TrainingEvaluation extends Model<TrainingEvaluation> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("empathy_score")
    private BigDecimal empathyScore;

    @TableField("communication_score")
    private BigDecimal communicationScore;

    @TableField("problem_solving_score")
    private BigDecimal problemSolvingScore;

    @TableField("emotional_recognition_score")
    private BigDecimal emotionalRecognitionScore;

    @TableField("strengths")
    private String strengths;

    @TableField("areas_for_improvement")
    private String areasForImprovement;

    @TableField("ai_comprehensive_comment")
    private String aiComprehensiveComment;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
