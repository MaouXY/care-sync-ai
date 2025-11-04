package com.caresync.ai.model.json;

import lombok.Data;

import java.util.List;

/**
 * AI建议（对应ai_assist_scheme表的ai_suggestions字段）
 */
@Data
public class AiSuggestions {
    private List<String> targetSuggest; // 建议目标
    private List<String> measuresSuggest; // 建议措施
}
