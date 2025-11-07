package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.AiAnalysisQueryDTO;
import com.caresync.ai.model.VO.AiAnalysisResultVO;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IAiAnalysisLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI分析模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/ai/analysis")
@Tag(name = "AI分析模块接口", description = "儿童AI分析结果相关接口")
public class AiAnalysisSchemeController {

    //TODO 根据id使用ai分析具体儿童的结果

    @Autowired
    private IAiAnalysisLogService aiAnalysisLogService;

    /**
     * 获取AI分析结果列表
     * @param aiAnalysisQueryDTO 查询条件，包含儿童姓名、潜在问题、情感趋势等查询参数（均允许空值）
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "获取AI分析结果列表", description = "分页查询AI分析结果列表，支持按儿童姓名、潜在问题、情感趋势筛选")
    public Result<PageResult<AiAnalysisResultVO>> getAiAnalysisResults(AiAnalysisQueryDTO aiAnalysisQueryDTO) {
        // 调用service层方法获取AI分析结果列表
        PageResult<AiAnalysisResultVO> result = aiAnalysisLogService.getAiAnalysisResults(aiAnalysisQueryDTO);
        return Result.success(result);
    }

    /**
     * 获取AI分析结果详情
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取AI分析结果详情", description = "获取AI分析结果详情，包含潜在问题、情感趋势、核心需求、分析摘要、情绪趋势分析、情感指标分析、关键发现、建议与干预策略、AI结构化数据")
    public Result<AiAnalysisResultVO> getAiAnalysisDetail(@PathVariable Long id) {
        // 调用service层方法获取AI分析结果详情
        AiAnalysisResultVO result = aiAnalysisLogService.getAiAnalysisDetail(id);
        if (result == null) {
            return Result.error("未找到相关分析结果");
        }
        return Result.success(result);
    }

    /**
     * 根据儿童id生成AI分析结果
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    @PostMapping("/{id}")
    @Operation(summary = "根据儿童id生成AI分析结果", description = "根据儿童ID生成AI分析结果，包含潜在问题、情感趋势、核心需求、分析摘要、情绪趋势分析、情感指标分析、关键发现、建议与干预策略、AI结构化数据")
    public Result<AiAnalysisResultVO> generateAiAnalysis(@PathVariable Long id) {
        // 调用service层方法根据儿童id生成AI分析结果
        AiAnalysisResultVO result = aiAnalysisLogService.generateAiAnalysis(id);
        if (result == null) {
            return Result.error("生成AI分析结果失败");
        }
        return Result.success(result);
    }

}