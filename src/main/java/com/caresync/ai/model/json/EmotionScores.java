package com.caresync.ai.model.json;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "情感评分指标")
public class EmotionScores {
    @Schema(description = "情绪稳定性")
    private Integer stability;
    @Schema(description = "焦虑水平")
    private Integer anxiety;
    @Schema(description = "幸福感")
    private Integer happiness;
    @Schema(description = "社交自信")
    private Integer socialConfidence;
}
