package com.caresync.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.result.Result;
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.service.IAssistTrackLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  AI服务进度日志控制器
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Slf4j
@RestController
@RequestMapping("/api/social-worker/track-log")
public class AssistTrackLogController {

    @Autowired
    private IAssistTrackLogService assistTrackLogService;

    /**
     * 查询AI服务进度日志列表
     * 根据时间倒序排序
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<AssistTrackLog>> getTrackLogList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        try {
            // 创建分页对象
            Page<AssistTrackLog> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件，按创建时间倒序排序
            LambdaQueryWrapper<AssistTrackLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(AssistTrackLog::getCreateTime);
            
            // 执行查询
            Page<AssistTrackLog> result = assistTrackLogService.page(page, queryWrapper);
            
            log.info("查询AI服务进度日志列表成功，共查询到{}条记录", result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询AI服务进度日志列表失败", e);
            return Result.error("查询失败");
        }
    }

    /**
     * 根据服务方案ID查询进度日志
     * 根据时间倒序排序
     *
     * @param schemeId 服务方案ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/by-scheme/{schemeId}")
    public Result<Page<AssistTrackLog>> getTrackLogBySchemeId(
            @PathVariable("schemeId") Long schemeId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        try {
            // 创建分页对象
            Page<AssistTrackLog> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件，按服务方案ID筛选并按创建时间倒序排序
            LambdaQueryWrapper<AssistTrackLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssistTrackLog::getSchemeId, schemeId)
                       .orderByDesc(AssistTrackLog::getCreateTime);
            
            // 执行查询
            Page<AssistTrackLog> result = assistTrackLogService.page(page, queryWrapper);
            
            log.info("根据服务方案ID查询进度日志成功，方案ID：{}，共查询到{}条记录", schemeId, result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("根据服务方案ID查询进度日志失败，方案ID：{}", schemeId, e);
            return Result.error("查询失败");
        }
    }

    /**
     * 根据儿童ID查询进度日志
     * 根据时间倒序排序
     *
     * @param childId  儿童ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/by-child/{childId}")
    public Result<Page<AssistTrackLog>> getTrackLogByChildId(
            @PathVariable("childId") Long childId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        try {
            // 创建分页对象
            Page<AssistTrackLog> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件，按儿童ID筛选并按创建时间倒序排序
            LambdaQueryWrapper<AssistTrackLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssistTrackLog::getChildId, childId)
                       .orderByDesc(AssistTrackLog::getCreateTime);
            
            // 执行查询
            Page<AssistTrackLog> result = assistTrackLogService.page(page, queryWrapper);
            
            log.info("根据儿童ID查询进度日志成功，儿童ID：{}，共查询到{}条记录", childId, result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("根据儿童ID查询进度日志失败，儿童ID：{}", childId, e);
            return Result.error("查询失败");
        }
    }

    /**
     * 根据社工ID查询进度日志
     * 根据时间倒序排序
     *
     * @param workerId 社工ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/by-worker/{workerId}")
    public Result<Page<AssistTrackLog>> getTrackLogByWorkerId(
            @PathVariable("workerId") Long workerId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        try {
            // 创建分页对象
            Page<AssistTrackLog> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件，按社工ID筛选并按创建时间倒序排序
            LambdaQueryWrapper<AssistTrackLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssistTrackLog::getWorkerId, workerId)
                       .orderByDesc(AssistTrackLog::getCreateTime);
            
            // 执行查询
            Page<AssistTrackLog> result = assistTrackLogService.page(page, queryWrapper);
            
            log.info("根据社工ID查询进度日志成功，社工ID：{}，共查询到{}条记录", workerId, result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("根据社工ID查询进度日志失败，社工ID：{}", workerId, e);
            return Result.error("查询失败");
        }
    }

    /**
     * 根据完成状态查询进度日志
     * 根据时间倒序排序
     *
     * @param status   完成状态
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/by-status/{status}")
    public Result<Page<AssistTrackLog>> getTrackLogByStatus(
            @PathVariable("status") String status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        try {
            // 创建分页对象
            Page<AssistTrackLog> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件，按完成状态筛选并按创建时间倒序排序
            LambdaQueryWrapper<AssistTrackLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssistTrackLog::getCompletionStatus, status)
                       .orderByDesc(AssistTrackLog::getCreateTime);
            
            // 执行查询
            Page<AssistTrackLog> result = assistTrackLogService.page(page, queryWrapper);
            
            log.info("根据完成状态查询进度日志成功，状态：{}，共查询到{}条记录", status, result.getTotal());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("根据完成状态查询进度日志失败，状态：{}", status, e);
            return Result.error("查询失败");
        }
    }
}