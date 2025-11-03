package com.caresync.ai.controller;

import com.caresync.ai.model.VO.AiAnalysisResultVO;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * AI分析模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/ai/analysis")
@Tag(name = "AI分析模块接口", description = "儿童AI分析结果相关接口")
public class AiAnalysisController {

    /**
     * 获取AI分析结果列表
     * @param childId 儿童ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "获取AI分析结果列表", description = "获取指定儿童的AI分析结果")
    public Result<PageResult<AiAnalysisResultVO>> getAiAnalysisResults(@RequestParam Long childId,
                                                                    @RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 获取AI分析结果详情
     * @param id 分析结果ID
     * @return AI分析结果VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取AI分析结果详情", description = "获取AI分析结果详情")
    public Result<AiAnalysisResultVO> getAiAnalysisDetail(@PathVariable Long id) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }
}