package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.*;
import com.caresync.ai.model.VO.*;
import com.caresync.ai.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI模拟训练模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/simulation")
@Tag(name = "AI模拟训练模块接口", description = "AI模拟留守儿童训练相关接口")
public class AiSimulationController {

    /**
     * 获取模拟训练场景列表
     * @return 场景列表
     */
    @GetMapping("/scenarios")
    @Operation(summary = "获取模拟训练场景列表", description = "获取所有可用的模拟训练场景列表")
    public Result<List<SimulationScenarioVO>> getScenarioList() {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 开始新的训练会话
     * @param startTrainingSessionDTO 开始训练会话DTO
     * @return 训练会话VO
     */
    @PostMapping("/start")
    @Operation(summary = "开始新的训练会话", description = "选择场景并开始一个新的训练会话")
    public Result<TrainingSessionVO> startTrainingSession(@RequestBody StartTrainingSessionDTO startTrainingSessionDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 发送训练消息
     * @param sendTrainingMessageDTO 发送训练消息DTO
     * @return 消息VO
     */
    @PostMapping("/send")
    @Operation(summary = "发送训练消息", description = "社工向模拟儿童发送消息")
    public Result<ChatMessageVO> sendTrainingMessage(@RequestBody SendTrainingMessageDTO sendTrainingMessageDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 获取训练会话记录
     * @param sessionId 会话ID
     * @return 会话记录列表
     */
    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取训练会话记录", description = "获取指定训练会话的完整聊天记录")
    public Result<List<ChatMessageVO>> getTrainingSessionHistory(@PathVariable String sessionId) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 获取AI指导意见
     * @param sessionId 会话ID
     * @return AI指导意见VO
     */
    @GetMapping("/advice/{sessionId}")
    @Operation(summary = "获取AI指导意见", description = "获取AI对当前训练会话的指导意见")
    public Result<AiGuidanceVO> getAiGuidance(@PathVariable String sessionId) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 结束训练会话
     * @param sessionId 会话ID
     * @param endTrainingSessionDTO 结束训练会话DTO
     * @return 训练评估VO
     */
    @PostMapping("/end/{sessionId}")
    @Operation(summary = "结束训练会话", description = "结束训练会话并获取总结评估")
    public Result<TrainingEvaluationVO> endTrainingSession(@PathVariable String sessionId, 
                                                          @RequestBody EndTrainingSessionDTO endTrainingSessionDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
}