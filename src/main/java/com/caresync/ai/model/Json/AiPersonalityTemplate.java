package com.caresync.ai.model.Json;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI个性模板（对应simulation_scenario表的ai_personality_template字段）
 */
@Data
public class AiPersonalityTemplate {
    private Integer age; // 模拟儿童年龄
    private String gender; // 性别
    private List<String> personalityTraits; // 性格特征
    private String speechPattern; // 说话模式
    private String emotionalState; // 情绪状态
    private String backgroundStory; // 背景故事
    private List<String> interests; // 兴趣爱好
    private List<String> fears; // 恐惧点
    private CommunicationPreferences communicationPreferences; // 沟通偏好

    @Data
    public static class CommunicationPreferences {
        private List<String> preferredTopics; // 偏好话题
        private List<String> avoidTopics; // 避免话题
        private String responseStyle; // 回应风格
    }
}
