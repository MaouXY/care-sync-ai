package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.AddTrackLogDTO;
import com.caresync.ai.model.DTO.UpdateTrackLogDTO;
import com.caresync.ai.model.VO.TrackLogVO;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 帮扶跟踪日志模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/track")
@Tag(name = "帮扶跟踪模块接口", description = "帮扶方案跟踪日志相关接口")
public class AssistTrackController {

    /**
     * 获取帮扶跟踪日志列表
     * @param schemeId 方案ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/logs")
    @Operation(summary = "获取帮扶跟踪日志列表", description = "获取指定方案的帮扶跟踪日志")
    public Result<PageResult<TrackLogVO>> getTrackLogs(@RequestParam Long schemeId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 添加帮扶跟踪日志
     * @param addTrackLogDTO 添加日志DTO
     * @return 结果
     */
    @PostMapping("/log")
    @Operation(summary = "添加帮扶跟踪日志", description = "添加新的帮扶跟踪日志")
    public Result addTrackLog(@RequestBody AddTrackLogDTO addTrackLogDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 更新帮扶跟踪日志
     * @param id 日志ID
     * @param updateTrackLogDTO 更新日志DTO
     * @return 结果
     */
    @PutMapping("/log/{id}")
    @Operation(summary = "更新帮扶跟踪日志", description = "更新帮扶跟踪日志内容")
    public Result updateTrackLog(@PathVariable Long id, @RequestBody UpdateTrackLogDTO updateTrackLogDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
}