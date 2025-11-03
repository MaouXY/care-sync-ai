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
 * @since 2025-11-04
 */
@Getter
@Setter
@TableName("simulation_scenario")
public class SimulationScenario extends Model<SimulationScenario> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("scenario_name")
    private String scenarioName;

    @TableField("scenario_type")
    private String scenarioType;

    @TableField("description")
    private String description;

    @TableField("difficulty_level")
    private Integer difficultyLevel;

    @TableField("target_skill")
    private String targetSkill;

    @TableField("ai_personality_template")
    private Object aiPersonalityTemplate;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
