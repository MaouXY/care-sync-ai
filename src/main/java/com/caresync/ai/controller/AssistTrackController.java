package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.AddTrackLogDTO;
import com.caresync.ai.model.DTO.SchemeLogDTO;
import com.caresync.ai.model.DTO.SchemeLogQueryDTO;
import com.caresync.ai.model.DTO.UpdateTrackLogDTO;
import com.caresync.ai.model.VO.*;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.model.json.MeasuresSuggest;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.IAssistTrackLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    private IAssistTrackLogService assistTrackLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取服务详情
     */
    @GetMapping("/scheme/{id}")
    @Operation(summary = "获取服务详情", description = "根据服务方案ID获取详细信息")
    public Result<AssistSchemeLogVO> getSchemeDetail(@PathVariable Long id) {
        try {
            log.info("开始获取服务方案详情，方案ID: {}", id);

            // 1. 参数校验
            if (id == null) {
                log.warn("服务方案ID不能为空");
                return Result.error("参数不能为空");
            }

            // 2. 调用服务获取方案详情
            AssistSchemeLogVO logVO = assistTrackLogService.getScheme(new SchemeLogDTO(id));

            log.info("成功获取服务方案详情，方案ID: {}", id);
            return Result.success(logVO);
        } catch (Exception e) {
            log.error("获取服务方案详情异常，方案ID: {}", id, e);
            return Result.error("获取服务方案详情异常: " + e.getMessage());
        }
    }

    /**
     * 添加服务跟踪日志
     *
     * @param addTrackLogDTO 添加日志DTO
     * @return 结果
     */
    //@PostMapping("/log")
    //@Operation(summary = "添加服务跟踪日志", description = "添加新的服务跟踪日志")
    public Result addTrackLog(@RequestBody AddTrackLogDTO addTrackLogDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 更新服务跟踪日志
     *
     * @param id                日志ID
     * @param updateTrackLogDTO 更新日志DTO
     * @return 结果
     */
    @PutMapping("/log/{id}")
    @Operation(summary = "更新服务跟踪日志", description = "更新服务跟踪日志内容")
    @Transactional
    public Result updateTrackLog(@PathVariable Long id, @RequestBody UpdateTrackLogDTO updateTrackLogDTO) {
        try {
            log.info("开始更新服务跟踪日志，日志ID: {}, 更新数据: {}", id, updateTrackLogDTO);

            // 1. 验证参数
            if (id == null || updateTrackLogDTO == null) {
                log.warn("更新服务跟踪日志参数为空，日志ID: {}, DTO: {}", id, updateTrackLogDTO);
                return Result.error("参数不能为空");
            }

            // 2. 查询日志记录
            AssistTrackLog trackLog = assistTrackLogService.getById(id);
            if (trackLog == null) {
                log.warn("未找到服务跟踪日志记录，日志ID: {}", id);
                return Result.error("未找到指定的服务跟踪日志");
            }

            // 3. 查询关联的服务方案
            AiAssistScheme scheme = aiAssistSchemeService.getById(trackLog.getSchemeId());
            if (scheme == null) {
                log.warn("未找到关联的服务方案，方案ID: {}", trackLog.getSchemeId());
                return Result.error("未找到关联的服务方案");
            }

            // 4. 更新日志记录
            boolean logUpdated = updateTrackLogRecord(trackLog, updateTrackLogDTO);
            if (!logUpdated) {
                log.error("更新服务跟踪日志记录失败，日志ID: {}", id);
                return Result.error("更新服务跟踪日志失败");
            }

            // 5. 同步更新服务方案中的子任务状态
            boolean schemeUpdated = updateSchemeTaskStatus(scheme, trackLog, updateTrackLogDTO);
            if (!schemeUpdated) {
                log.error("更新服务方案子任务状态失败，方案ID: {}, 日志ID: {}", scheme.getId(), id);
                return Result.error("更新服务方案子任务状态失败");
            }

            log.info("服务跟踪日志更新成功，日志ID: {}, 方案ID: {}", id, scheme.getId());
            return Result.success();

        } catch (Exception e) {
            log.error("更新服务跟踪日志异常，日志ID: {}", id, e);
            return Result.error("更新服务跟踪日志异常: " + e.getMessage());
        }
    }

    /**
     * 更新服务跟踪日志记录
     */
    private boolean updateTrackLogRecord(AssistTrackLog trackLog, UpdateTrackLogDTO updateTrackLogDTO) {
        try {
            // 更新完成状态
            if (updateTrackLogDTO.getCompletionStatus() != null) {
                trackLog.setCompletionStatus(updateTrackLogDTO.getCompletionStatus());
            }

            // 更新记录内容
            if (updateTrackLogDTO.getRecordContent() != null) {
                trackLog.setRecordContent(updateTrackLogDTO.getRecordContent());
            }

            // 设置更新时间
            trackLog.setUpdateTime(LocalDateTime.now());

            // 保存更新
            boolean result = assistTrackLogService.updateById(trackLog);
            log.debug("更新服务跟踪日志记录结果: {}, 日志ID: {}", result, trackLog.getId());
            return result;

        } catch (Exception e) {
            log.error("更新服务跟踪日志记录异常，日志ID: {}", trackLog.getId(), e);
            return false;
        }
    }

    /**
     * 更新服务方案中的子任务状态
     */
    private boolean updateSchemeTaskStatus(AiAssistScheme scheme, AssistTrackLog trackLog, UpdateTrackLogDTO updateTrackLogDTO) {
        try {
            // 获取ai_suggestions
            Object aiSuggestionsObj = scheme.getAiSuggestions();
            if (aiSuggestionsObj == null) {
                log.warn("服务方案ai_suggestions为空，方案ID: {}", scheme.getId());
                return false;
            }

            // 解析JSON到DetailSchemeVO对象
            DetailSchemeVO detailSchemeVO = parseAiSuggestionsToDetailSchemeVO(aiSuggestionsObj);
            if (detailSchemeVO == null || detailSchemeVO.getMeasuresSuggest() == null) {
                log.warn("服务方案measures_suggest格式不正确，方案ID: {}", scheme.getId());
                return false;
            }

            boolean taskUpdated = false;

            // 遍历measures_suggest，找到对应的子任务
            for (DetailSchemeVO.WeeklyMeasure measure : detailSchemeVO.getMeasuresSuggest()) {
                if (measure.getDetails() == null) {
                    continue;
                }

                // 遍历details，找到对应的子任务
                for (DetailSchemeVO.TaskDetail detail : measure.getDetails()) {
                    Long trackLogId = detail.getAssistTrackLogId();

                    // 找到对应的子任务
                    if (trackLogId != null && trackLogId.equals(trackLog.getId())) {
                        // 更新子任务状态
                        if (updateTrackLogDTO.getCompletionStatus() != null) {
                            // 转换状态值：COMPLETED -> completed, UNFINISHED -> pending
                            String status = convertCompletionStatus(updateTrackLogDTO.getCompletionStatus());
                            detail.setStatus(status);
                            log.debug("更新子任务状态，日志ID: {}, 状态: {}", trackLogId, status);
                        }

                        // 更新记录内容
                        if (updateTrackLogDTO.getRecordContent() != null) {
                            detail.setContent(updateTrackLogDTO.getRecordContent());
                            log.debug("更新子任务内容，日志ID: {}, 内容: {}", trackLogId, updateTrackLogDTO.getRecordContent());
                        }

                        taskUpdated = true;
                        break;
                    }
                }

                if (taskUpdated) {
                    break;
                }
            }

            if (taskUpdated) {
                // 将更新后的DetailSchemeVO转换回JSON字符串
                String updatedAiSuggestions = objectMapper.writeValueAsString(detailSchemeVO);
                scheme.setAiSuggestions(updatedAiSuggestions);

                // 保存更新
                boolean result = aiAssistSchemeService.updateById(scheme);
                log.debug("更新服务方案子任务状态结果: {}, 方案ID: {}", result, scheme.getId());
                return result;
            } else {
                log.warn("未找到对应的子任务，日志ID: {}, 方案ID: {}", trackLog.getId(), scheme.getId());
                return false;
            }

        } catch (Exception e) {
            log.error("更新服务方案子任务状态异常，方案ID: {}, 日志ID: {}", scheme.getId(), trackLog.getId(), e);
            return false;
        }
    }

    /**
     * 将ai_suggestions解析为DetailSchemeVO对象
     */
    private DetailSchemeVO parseAiSuggestionsToDetailSchemeVO(Object aiSuggestionsObj) {
        try {
            if (aiSuggestionsObj == null) {
                return null;
            }

            String aiSuggestions;
            if (aiSuggestionsObj instanceof String) {
                aiSuggestions = (String) aiSuggestionsObj;
            } else {
                // 如果不是String类型，转换为JSON字符串
                aiSuggestions = objectMapper.writeValueAsString(aiSuggestionsObj);
            }

            // 解析JSON到Map
            Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {
            });

            // 创建DetailSchemeVO对象
            DetailSchemeVO detailSchemeVO = new DetailSchemeVO();

            // 提取target_suggest
            Object targetSuggest = aiSuggestionsMap.get("target_suggest");
            if (targetSuggest instanceof List) {
                detailSchemeVO.setTargetSuggest((List<String>) targetSuggest);
            }

            // 提取measures_suggest并转换为WeeklyMeasure列表
            Object measuresSuggest = aiSuggestionsMap.get("measures_suggest");
            if (measuresSuggest instanceof List) {
                List<Map<String, Object>> measuresList = (List<Map<String, Object>>) measuresSuggest;
                List<DetailSchemeVO.WeeklyMeasure> weeklyMeasures = new ArrayList<>();

                for (Map<String, Object> measure : measuresList) {
                    DetailSchemeVO.WeeklyMeasure weeklyMeasure = new DetailSchemeVO.WeeklyMeasure();

                    // 设置周数
                    String week = (String) measure.get("week");
                    weeklyMeasure.setWeek(week);

                    // 设置任务详情
                    Object detailsObj = measure.get("details");
                    if (detailsObj instanceof List) {
                        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                        List<DetailSchemeVO.TaskDetail> taskDetails = new ArrayList<>();

                        for (Map<String, Object> detail : detailsList) {
                            DetailSchemeVO.TaskDetail taskDetail = new DetailSchemeVO.TaskDetail();

                            // 设置任务内容
                            String content = (String) detail.get("content");
                            taskDetail.setContent(content);

                            // 设置任务状态
                            String status = (String) detail.get("status");
                            taskDetail.setStatus(status != null ? status : "pending");

                            // 设置关联的跟踪日志ID
                            Object trackLogIdObj = detail.get("assist_track_log_id");
                            if (trackLogIdObj != null) {
                                if (trackLogIdObj instanceof Long) {
                                    taskDetail.setAssistTrackLogId((Long) trackLogIdObj);
                                } else if (trackLogIdObj instanceof Integer) {
                                    taskDetail.setAssistTrackLogId(((Integer) trackLogIdObj).longValue());
                                }
                            }

                            taskDetails.add(taskDetail);
                        }

                        weeklyMeasure.setDetails(taskDetails);
                    }

                    weeklyMeasures.add(weeklyMeasure);
                }

                detailSchemeVO.setMeasuresSuggest(weeklyMeasures);
            }

            return detailSchemeVO;

        } catch (Exception e) {
            log.error("解析ai_suggestions到DetailSchemeVO失败", e);
            return null;
        }
    }

    /**
     * 转换完成状态值
     */
    private String convertCompletionStatus(String completionStatus) {
        if ("COMPLETED".equals(completionStatus)) {
            return "completed";
        } else if ("UNFINISHED".equals(completionStatus)) {
            return "pending";
        } else {
            return completionStatus.toLowerCase();
        }
    }

    /**
     * 获取帮扶跟踪管理数据统计
     *
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
            trendData.put("date", "2024-" + (5 + i / 4) + "-" + (1 + (i % 4) * 7));
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
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/scheme/list")
    @Operation(summary = "获取服务方案列表", description = "获取服务方案列表，排除草稿状态")
    public Result<PageResult<AssistSchemeLogVO>> getSchemeList(@RequestBody SchemeLogQueryDTO queryDTO) {
        try {
            // 调用Service层方法获取方案列表
            PageResult<AssistSchemeLogVO> pageResult = assistTrackLogService.getSchemeList(queryDTO);
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

            // 解析JSON到DetailSchemeVO对象
            DetailSchemeVO detailSchemeVO = parseAiSuggestionsToDetailSchemeVO(aiSuggestionsObj);
            if (detailSchemeVO == null || detailSchemeVO.getMeasuresSuggest() == null) {
                log.warn("方案[{}]的measures_suggest格式不正确", scheme.getId());
                return 0;
            }

            log.debug("方案[{}]解析到{}个措施建议", scheme.getId(), detailSchemeVO.getMeasuresSuggest().size());
            int totalTasks = 0;
            double completedTasks = 0;

            // 遍历measures_suggest计算任务完成情况
            for (DetailSchemeVO.WeeklyMeasure measure : detailSchemeVO.getMeasuresSuggest()) {
                if (measure.getDetails() != null) {
                    for (DetailSchemeVO.TaskDetail detail : measure.getDetails()) {
                        totalTasks++;
                        String status = detail.getStatus() != null ? detail.getStatus() : "pending";

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


    /**
     * 将DetailSchemeVO转换为AssistSchemeLogVO
     */
    private AssistSchemeLogVO convertToSchemeLogVO(DetailSchemeVO detailVO) {
        AssistSchemeLogVO logVO = new AssistSchemeLogVO();

        // 基本信息
        logVO.setId(detailVO.getId());
        logVO.setTarget(detailVO.getTarget());
        logVO.setWorkerId(detailVO.getWorkerId());
        logVO.setWorkerName(detailVO.getWorkerName());
        logVO.setSchemeStatus(detailVO.getSchemeStatus());
        logVO.setWorkerAdjustReason(detailVO.getWorkerAdjustReason());
        logVO.setCreateTime(detailVO.getCreateTime());

        // 儿童信息
        if (detailVO.getChildInfo() != null) {
            DetailSchemeVO.ChildDetailInfo childInfo = detailVO.getChildInfo();
            logVO.setChildId(childInfo.getId());
            logVO.setChildName(childInfo.getName());
            if (childInfo.getAge() != null) {
                logVO.setChildAge(childInfo.getAge().toString());
            }
        }

        // 方案目标建议
        if (detailVO.getTargetSuggest() != null) {
            logVO.setTargetSuggest(detailVO.getTargetSuggest().toArray(new String[0]));
        }

        // 方案措施建议
        if (detailVO.getMeasuresSuggest() != null) {
            List<MeasuresSuggest> measuresSuggestList = new ArrayList<>();
            for (DetailSchemeVO.WeeklyMeasure weeklyMeasure : detailVO.getMeasuresSuggest()) {
                MeasuresSuggest measuresSuggest = new MeasuresSuggest();
                measuresSuggest.setWeek(weeklyMeasure.getWeek());

                if (weeklyMeasure.getDetails() != null) {
                    List<com.caresync.ai.model.json.MeasuresSuggestDetails> detailsList = new ArrayList<>();
                    for (DetailSchemeVO.TaskDetail taskDetail : weeklyMeasure.getDetails()) {
                        com.caresync.ai.model.json.MeasuresSuggestDetails details = new com.caresync.ai.model.json.MeasuresSuggestDetails();
                        details.setContent(taskDetail.getContent());
                        details.setStatus(taskDetail.getStatus());
                        details.setAssist_track_log_id(taskDetail.getAssistTrackLogId());
                        detailsList.add(details);
                    }
                    measuresSuggest.setDetails(detailsList.toArray(new com.caresync.ai.model.json.MeasuresSuggestDetails[0]));
                }

                measuresSuggestList.add(measuresSuggest);
            }
            logVO.setMeasuresSuggest(measuresSuggestList.toArray(new MeasuresSuggest[0]));
        }

        return logVO;
    }
}