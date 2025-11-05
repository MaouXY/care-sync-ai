package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;

/**
 * 模拟场景基本信息响应结果
 */
@Data
public class SimulationScenarioVO implements Serializable {
    private Long id; // 场景ID
    private String scenarioName; // 场景名称
    private String scenarioType; // 场景类型
    private String description; // 场景描述
    private Integer difficultyLevel; // 难度等级
}