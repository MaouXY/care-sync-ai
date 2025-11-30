package com.caresync.ai.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI建议（对应ai_assist_scheme表的ai_suggestions字段）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiSuggestions {
    @JsonProperty("target_suggest")
    private List<String> targetSuggest; // 建议目标
    
    @JsonProperty("measures_suggest")
    private List<String> measuresSuggest; // 建议措施
}