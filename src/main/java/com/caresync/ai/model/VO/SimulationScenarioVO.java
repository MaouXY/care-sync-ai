package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 模拟场景响应结果
 */
@Data
public class SimulationScenarioVO implements Serializable {
    private Long id; // 场景ID
    private String scenarioName; // 场景名称
    private String scenarioType; // 场景类型
    private String description; // 场景描述
    private Integer difficultyLevel; // 难度等级
    private String targetSkill; // 目标技能
    private Map<String, Object> aiPersonalityTemplate; // AI个性模板
    private Boolean isPublic; // 是否公开场景
    private Long createdBy; // 创建者ID
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

