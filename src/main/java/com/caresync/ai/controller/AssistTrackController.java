package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.AddTrackLogDTO;
import com.caresync.ai.model.DTO.UpdateTrackLogDTO;
import com.caresync.ai.model.VO.TrackLogVO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IChildService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务跟踪日志模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/social-worker/track")
@Tag(name = "服务跟踪模块接口", description = "服务方案跟踪日志相关接口")
public class AssistTrackController {

    @Autowired
    private IAiAssistSchemeService aiAssistSchemeService;
    
    @Autowired
    private IChildService childService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取服务跟踪日志列表
     * @param schemeId 方案ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/logs")
    @Operation(summary = "获取服务跟踪日志列表", description = "获取指定方案的服务跟踪日志")
    public Result<PageResult<TrackLogVO>> getTrackLogs(@RequestParam Long schemeId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 添加服务跟踪日志
     * @param addTrackLogDTO 添加日志DTO
     * @return 结果
     */
    @PostMapping("/log")
    @Operation(summary = "添加服务跟踪日志", description = "添加新的服务跟踪日志")
    public Result addTrackLog(@RequestBody AddTrackLogDTO addTrackLogDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 更新服务跟踪日志
     * @param id 日志ID
     * @param updateTrackLogDTO 更新日志DTO
     * @return 结果
     */
    @PutMapping("/log/{id}")
    @Operation(summary = "更新服务跟踪日志", description = "更新服务跟踪日志内容")
    public Result updateTrackLog(@PathVariable Long id, @RequestBody UpdateTrackLogDTO updateTrackLogDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
    
    /**
     * 获取帮扶跟踪管理数据统计
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取帮扶跟踪管理数据统计", description = "获取服务方案数量、状态分布等统计信息")
    public Result<Map<String, Object>> getTrackStatistics() {
        // 查询所有方案（排除DRAFT状态）
        List<AiAssistScheme> allSchemes = aiAssistSchemeService.list();
        List<AiAssistScheme> validSchemes = allSchemes.stream()
                .filter(scheme -> !"DRAFT".equals(scheme.getSchemeStatus()))
                .collect(Collectors.toList());
        
        // 计算总方案数
        int totalSchemeCount = validSchemes.size();
        
        // 计算进行中方案数
        int inProgressCount = (int) validSchemes.stream()
                .filter(scheme -> "IN_PROGRESS".equals(scheme.getSchemeStatus()))
                .count();
        
        // 计算已完成方案数
        int completedCount = (int) validSchemes.stream()
                .filter(scheme -> "COMPLETED".equals(scheme.getSchemeStatus()))
                .count();
        
        // 计算进行中/已完成的百分比
        double progressPercentage = 0;
        if (completedCount > 0) {
            progressPercentage = Math.round(((double) inProgressCount / completedCount) * 100);
        }
        
        // 准备状态分布数据
        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("IN_PROGRESS", inProgressCount);
        statusDistribution.put("COMPLETED", completedCount);
        
        // 准备服务进度趋势数据（使用假数据）
        List<Map<String, Object>> progressTrend = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> trendData = new HashMap<>();
            trendData.put("date", "2024-" + (5 + i/4) + "-" + (1 + (i%4)*7));
            trendData.put("completed", 5 + i * 2);
            trendData.put("inProgress", 10 + i * 1);
            progressTrend.add(trendData);
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalSchemeCount", totalSchemeCount);
        result.put("inProgressCount", inProgressCount);
        result.put("completedCount", completedCount);
        result.put("progressPercentage", progressPercentage);
        result.put("statusDistribution", statusDistribution);
        result.put("progressTrend", progressTrend);
        
        return Result.success(result);
    }
    
    /**
     * 获取服务方案列表（排除DRAFT状态）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/scheme/list")
    @Operation(summary = "获取服务方案列表", description = "获取服务方案列表，排除草稿状态")
    public Result<PageResult<Map<String, Object>>> getSchemeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            // 验证页码和每页条数
            if (page < 1 || pageSize < 1) {
                log.warn("无效的分页参数: page={}, pageSize={}", page, pageSize);
                return Result.success(new PageResult<>(0L, Collections.emptyList()));
            }
            
            // 查询所有方案（排除DRAFT状态）
            List<AiAssistScheme> allSchemes = aiAssistSchemeService.list();
            log.debug("查询到的方案总数: {}", allSchemes.size());
            
            List<AiAssistScheme> validSchemes = allSchemes.stream()
                    .filter(scheme -> !"DRAFT".equals(scheme.getSchemeStatus()))
                    .collect(Collectors.toList());
            log.debug("有效的方案数量（排除DRAFT状态）: {}", validSchemes.size());
            
            // 分页处理
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, validSchemes.size());
            List<AiAssistScheme> pageSchemes;
            if (startIndex >= validSchemes.size()) {
                pageSchemes = Collections.emptyList();
            } else {
                pageSchemes = validSchemes.subList(startIndex, endIndex);
            }
            log.debug("当前页（第{}页）方案数量: {}", page, pageSchemes.size());
            
            // 转换为包含详细信息的列表
            List<Map<String, Object>> schemeList = new ArrayList<>();
            for (AiAssistScheme scheme : pageSchemes) {
                Map<String, Object> schemeInfo = new HashMap<>();
                schemeInfo.put("id", scheme.getId());
                schemeInfo.put("target", scheme.getTarget());
                schemeInfo.put("schemeStatus", scheme.getSchemeStatus());
                
                // 获取儿童信息
                Child child = childService.getById(scheme.getChildId());
                if (child != null) {
                    schemeInfo.put("childId", child.getId());
                    schemeInfo.put("childName", child.getName());
                    schemeInfo.put("childAge", child.getAge());
                } else {
                    schemeInfo.put("childId", scheme.getChildId());
                    schemeInfo.put("childName", "未找到该儿童");
                    schemeInfo.put("childAge", null);
                    log.warn("方案[{}]关联的儿童ID[{}]不存在", scheme.getId(), scheme.getChildId());
                }
                
                // 计算完成进度
                double completionRate = calculateCompletionRate(scheme);
                schemeInfo.put("completionRate", completionRate);
                
                // 计算剩余时间
                int remainingDays = calculateRemainingDays(scheme);
                schemeInfo.put("remainingDays", remainingDays);
                
                schemeList.add(schemeInfo);
            }
            
            // 构建分页结果
            PageResult<Map<String, Object>> pageResult = new PageResult<>(
                    (long) validSchemes.size(), schemeList);
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取服务方案列表失败", e);
            return Result.error("获取服务方案列表失败");
        }
    }
    
    /**
     * 计算方案完成进度
     */
    private double calculateCompletionRate(AiAssistScheme scheme) {
        try {
            // 如果方案已完成，直接返回100%
            if ("COMPLETED".equals(scheme.getSchemeStatus())) {
                return 100;
            }
            
            // 解析ai_suggestions
            Object aiSuggestionsObj = scheme.getAiSuggestions();
            if (aiSuggestionsObj == null) {
                log.warn("方案[{}]的aiSuggestions为空", scheme.getId());
                return 0;
            }
            
            String aiSuggestions;
            if (aiSuggestionsObj instanceof String) {
                aiSuggestions = (String) aiSuggestionsObj;
                if (aiSuggestions.isEmpty()) {
                    log.warn("方案[{}]的aiSuggestions为空字符串", scheme.getId());
                    return 0;
                }
            } else {
                // 如果不是String类型，尝试转换为JSON字符串
                try {
                    aiSuggestions = objectMapper.writeValueAsString(aiSuggestionsObj);
                } catch (Exception e) {
                    log.error("方案[{}]的aiSuggestions转换为字符串失败", scheme.getId(), e);
                    return 0;
                }
            }
            
            log.debug("开始解析方案[{}]的aiSuggestions: {}", scheme.getId(), aiSuggestions);
            Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {});
            Object measuresSuggest = aiSuggestionsMap.get("measures_suggest");
            
            if (measuresSuggest instanceof List) {
                List<Map<String, Object>> measuresList = (List<Map<String, Object>>) measuresSuggest;
                log.debug("方案[{}]解析到{}个措施建议", scheme.getId(), measuresList.size());
                int totalTasks = 0;
                int completedTasks = 0;
                
                for (Map<String, Object> measure : measuresList) {
                    Object detailsObj = measure.get("details");
                    if (detailsObj instanceof List) {
                        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                        
                        for (Map<String, Object> detail : detailsList) {
                            totalTasks++;
                            // 安全获取status字段
                            Object statusObj = detail.get("status");
                            String status = statusObj != null ? statusObj.toString() : "pending";
                            
                            if ("completed".equals(status)) {
                                completedTasks++;
                            } else if ("in_progress".equals(status)) {
                                // 进行中的任务算完成一半
                                completedTasks += 0.5;
                            }
                            
                            log.trace("任务状态: {}, 累计完成任务: {}, 总任务数: {}", status, completedTasks, totalTasks);
                        }
                    }
                }
                
                if (totalTasks > 0) {
                    double rate = Math.round((completedTasks / totalTasks) * 100);
                    log.debug("方案[{}]完成进度: {}% (完成任务: {}, 总任务数: {})", 
                            scheme.getId(), rate, completedTasks, totalTasks);
                    return rate;
                } else {
                    log.warn("方案[{}]没有找到任务数据", scheme.getId());
                }
            } else {
                log.warn("方案[{}]的measures_suggest格式不正确，不是List类型", scheme.getId());
            }
        } catch (Exception e) {
            log.error("计算方案[{}]完成进度失败", scheme.getId(), e);
        }
        
        return 0;
    }
    
    /**
     * 计算方案剩余时间
     */
    private int calculateRemainingDays(AiAssistScheme scheme) {
        try {
            // 如果方案已完成，剩余时间为0
            if ("COMPLETED".equals(scheme.getSchemeStatus())) {
                log.debug("方案[{}]已完成，剩余时间为0", scheme.getId());
                return 0;
            }
            
            // 获取更新时间
            LocalDateTime updateTime = scheme.getUpdateTime();
            if (updateTime == null) {
                log.warn("方案[{}]的updateTime为空，使用当前时间作为基准", scheme.getId());
                updateTime = LocalDateTime.now();
            }
            
            // 获取周期天数
            Integer cycle = scheme.getCycle() != null ? scheme.getCycle() : 7;
            log.debug("方案[{}]的更新时间: {}, 周期: {}天", scheme.getId(), updateTime, cycle);
            
            // 计算结束时间
            LocalDateTime endTime = updateTime.plusDays(cycle);
            
            // 计算剩余天数
            long remainingDays = LocalDateTime.now().until(endTime, ChronoUnit.DAYS);
            int result = Math.max(0, (int) remainingDays);
            log.debug("方案[{}]的结束时间: {}, 剩余天数: {}", scheme.getId(), endTime, result);
            
            return result;
        } catch (Exception e) {
            log.error("计算方案[{}]剩余时间失败", scheme.getId(), e);
            return 0;
        }
    }
}