package com.caresync.ai.model.Json;

import lombok.Data;

import java.util.List;

/**
 * 情感分析结果（对应training_chat_record表的emotion_analysis字段）
 */
@Data
public class EmotionAnalysis {
    private List<DetectedEmotion> detectedEmotions; // 检测到的情绪
    private Integer emotionIntensity; // 情绪强度

    @Data
    public static class DetectedEmotion {
        private String emotion; // 情绪类型
        private Integer confidence; // 置信度
    }
}
