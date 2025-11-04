package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * AI分析结果列表查询请求参数
 */
@Data
public class AiAnalysisQueryDTO {
    private String name; // 儿童姓名（模糊查询，允许空值）
    private String potentialProblems; // 潜在问题（模糊查询，允许空值）
    private String emotionTrend; // 情感趋势（模糊查询，允许空值）
    private Integer page=1; // 页码
    private Integer pageSize=10; // 每页条数
}