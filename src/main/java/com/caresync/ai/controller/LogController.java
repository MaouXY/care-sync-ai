package com.caresync.ai.controller;

import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/log")
@Tag(name = "日志模块接口", description = "系统日志、操作日志、登录日志相关接口")
public class LogController {

    /**
     * 获取系统日志
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/system")
    @Operation(summary = "获取系统日志", description = "分页查询系统日志")
    public Result<PageResult<?>> getSystemLog(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 获取操作日志
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/operation")
    @Operation(summary = "获取操作日志", description = "分页查询操作日志")
    public Result<PageResult<?>> getOperationLog(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 获取登录日志
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/login")
    @Operation(summary = "获取登录日志", description = "分页查询登录日志")
    public Result<PageResult<?>> getLoginLog(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
}