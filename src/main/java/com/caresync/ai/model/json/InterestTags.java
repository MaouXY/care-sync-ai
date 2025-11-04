package com.caresync.ai.model.json;

import lombok.Data;

import java.util.List;

/**
 * 兴趣标签（对应child表的interest_tags字段）
 */
@Data
public class InterestTags {
    private List<String> tags; // 兴趣标签列表
}

