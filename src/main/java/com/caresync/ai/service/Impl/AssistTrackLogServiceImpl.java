package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caresync.ai.model.DTO.SchemeLogQueryDTO;
import com.caresync.ai.model.DTO.SchemeLogDTO;
import com.caresync.ai.model.VO.AssistSchemeLogVO;
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AssistTrackLogMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IAssistTrackLogService;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IChildService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Slf4j
@Service
public class AssistTrackLogServiceImpl extends ServiceImpl<AssistTrackLogMapper, AssistTrackLog> implements IAssistTrackLogService {

    @Autowired
    private IAiAssistSchemeService aiAssistSchemeService;
    
    @Autowired
    private IChildService childService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取服务方案日志（排除DRAFT状态）
     */
    @Override
    public AssistSchemeLogVO getScheme(SchemeLogDTO queryDTO) {
         // 执行查询
        AiAssistScheme trackLog = aiAssistSchemeService.getById(queryDTO.getId());

         if (trackLog == null) {
             log.warn("未找到ID为 {} 的服务方案日志", queryDTO.getId());
             return null;
         }
         // 转换为VO
         List<AssistSchemeLogVO> vo = convertToSchemeVOList( List.of(trackLog));
         return vo.get(0);
    }

    /**
     * 获取服务方案列表（排除DRAFT状态）
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<AssistSchemeLogVO> getSchemeList(SchemeLogQueryDTO queryDTO) {
        try {
            // 设置分页参数
            int page = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
            int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

            // 使用PageHelper进行分页查询
            PageHelper.startPage(page, pageSize);

            // 构建查询条件
            LambdaQueryWrapper<AiAssistScheme> queryWrapper = buildQueryConditions(queryDTO);

            // 执行查询
            List<AiAssistScheme> schemes = aiAssistSchemeService.list(queryWrapper);
            PageInfo<AiAssistScheme> pageInfo = new PageInfo<>(schemes);

            // 转换为VO列表
            List<AssistSchemeLogVO> schemeVOList = convertToSchemeVOList(schemes);

            // 构建并返回分页结果
            return new PageResult<>(pageInfo.getTotal(), schemeVOList);
        } catch (Exception e) {
            log.error("获取服务方案列表失败", e);
            return new PageResult<>(0L, Collections.emptyList());
        }
    }

    /**
     * 构建查询条件
     * @param queryDTO 查询条件DTO
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<AiAssistScheme> buildQueryConditions(SchemeLogQueryDTO queryDTO) {
        LambdaQueryWrapper<AiAssistScheme> queryWrapper = new LambdaQueryWrapper<>();

        // 排除DRAFT状态
        queryWrapper.ne(AiAssistScheme::getSchemeStatus, "DRAFT");

        // 按儿童ID过滤
        if (queryDTO.getChildId() != null) {
            queryWrapper.eq(AiAssistScheme::getChildId, queryDTO.getChildId());
        }

        // 按社工ID过滤
        if (queryDTO.getWorkerId() != null) {
            queryWrapper.eq(AiAssistScheme::getWorkerId, queryDTO.getWorkerId());
        }

        // 按服务目标过滤（模糊匹配）
        if (queryDTO.getTarget() != null && !queryDTO.getTarget().isEmpty()) {
            queryWrapper.like(AiAssistScheme::getTarget, queryDTO.getTarget());
        }

        // 按方案状态过滤
        if (queryDTO.getSchemeStatus() != null && !queryDTO.getSchemeStatus().isEmpty()) {
            queryWrapper.eq(AiAssistScheme::getSchemeStatus, queryDTO.getSchemeStatus());
        }

        // 按创建时间范围过滤
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(AiAssistScheme::getCreateTime, queryDTO.getStartDate());
        }

        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(AiAssistScheme::getCreateTime, queryDTO.getEndDate());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(AiAssistScheme::getCreateTime);

        return queryWrapper;
    }

    /**
     * 应用查询条件
     */
    private List<AiAssistScheme> applyQueryConditions(List<AiAssistScheme> schemes, SchemeLogQueryDTO queryDTO) {
        return schemes.stream()
                .filter(scheme -> {
                    // 按儿童ID过滤
                    if (queryDTO.getChildId() != null && !queryDTO.getChildId().equals(scheme.getChildId())) {
                        return false;
                    }
                    
                    // 按社工ID过滤
                    if (queryDTO.getWorkerId() != null && !queryDTO.getWorkerId().equals(scheme.getWorkerId())) {
                        return false;
                    }
                    
                    // 按服务目标过滤（模糊匹配）
                    if (queryDTO.getTarget() != null && !queryDTO.getTarget().isEmpty()) {
                        String target = scheme.getTarget();
                        if (target == null || !target.toLowerCase().contains(queryDTO.getTarget().toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // 按名称过滤（模糊匹配儿童名称）
                    if (queryDTO.getName() != null && !queryDTO.getName().isEmpty()) {
                        Child child = childService.getById(scheme.getChildId());
                        if (child == null || child.getName() == null || 
                            !child.getName().toLowerCase().contains(queryDTO.getName().toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // 按创建时间范围过滤
                    if (queryDTO.getStartDate() != null && scheme.getCreateTime() != null) {
                        if (scheme.getCreateTime().isBefore(queryDTO.getStartDate())) {
                            return false;
                        }
                    }
                    
                    if (queryDTO.getEndDate() != null && scheme.getCreateTime() != null) {
                        if (scheme.getCreateTime().isAfter(queryDTO.getEndDate())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO列表
     */
    private List<AssistSchemeLogVO> convertToSchemeVOList(List<AiAssistScheme> schemes) {
        List<AssistSchemeLogVO> voList = new ArrayList<>();

        for (AiAssistScheme scheme : schemes) {
            AssistSchemeLogVO vo = new AssistSchemeLogVO();

            // 设置基本信息
            vo.setId(scheme.getId());
            vo.setChildId(scheme.getChildId());
            vo.setWorkerId(scheme.getWorkerId());
            vo.setTarget(scheme.getTarget());
            vo.setCycle(scheme.getCycle());
            vo.setSchemeStatus(scheme.getSchemeStatus());
            vo.setWorkerAdjustReason(scheme.getWorkerAdjustReason());
            vo.setCreateTime(scheme.getCreateTime());
            vo.setUpdateTime(scheme.getUpdateTime());

            // 设置measures（服务tag）
            if (scheme.getMeasures() instanceof String[]) {
                vo.setMeasures(Arrays.asList((String[]) scheme.getMeasures()));
            } else if (scheme.getMeasures() instanceof List) {
                vo.setMeasures((List<String>) scheme.getMeasures());
            } else {
                vo.setMeasures(Collections.emptyList());
            }

            // 解析ai_suggestions获取targetSuggest和measuresSuggest
            parseAiSuggestions(scheme, vo);

            // 计算完成进度
            calculateCompletionRate(scheme, vo);

            // 获取儿童信息
            Child child = childService.getById(scheme.getChildId());
            if (child != null) {
                vo.setChildName(child.getName());
                vo.setChildAge(child.getAge() != null ? child.getAge().toString() : null);
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 解析ai_suggestions字段
     */
    private void parseAiSuggestions(AiAssistScheme scheme, AssistSchemeLogVO vo) {
        try {
            Object aiSuggestionsObj = scheme.getAiSuggestions();
            if (aiSuggestionsObj == null) {
                return;
            }
            
            String aiSuggestions;
            if (aiSuggestionsObj instanceof String) {
                aiSuggestions = (String) aiSuggestionsObj;
            } else {
                aiSuggestions = objectMapper.writeValueAsString(aiSuggestionsObj);
            }
            
            // 解析JSON到Map
            Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {});
            
            // 提取target_suggest
            Object targetSuggest = aiSuggestionsMap.get("target_suggest");
            if (targetSuggest instanceof List) {
                List<String> targetList = (List<String>) targetSuggest;
                vo.setTargetSuggest(targetList.toArray(new String[0]));
            }
            
            // 提取measures_suggest并转换为MeasuresSuggest数组
            Object measuresSuggest = aiSuggestionsMap.get("measures_suggest");
            if (measuresSuggest instanceof List) {
                List<Map<String, Object>> measuresList = (List<Map<String, Object>>) measuresSuggest;
                List<com.caresync.ai.model.json.MeasuresSuggest> measuresSuggestList = new ArrayList<>();
                
                for (Map<String, Object> measure : measuresList) {
                    com.caresync.ai.model.json.MeasuresSuggest measuresSuggestObj = new com.caresync.ai.model.json.MeasuresSuggest();
                    
                    // 设置周数
                    String week = (String) measure.get("week");
                    measuresSuggestObj.setWeek(week);
                    
                    // 设置任务详情
                    Object detailsObj = measure.get("details");
                    if (detailsObj instanceof List) {
                        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                        List<com.caresync.ai.model.json.MeasuresSuggestDetails> detailsSuggestList = new ArrayList<>();
                        
                        for (Map<String, Object> detail : detailsList) {
                            com.caresync.ai.model.json.MeasuresSuggestDetails detailObj = new com.caresync.ai.model.json.MeasuresSuggestDetails();
                            
                            // 设置任务内容
                            String content = (String) detail.get("content");
                            detailObj.setContent(content);
                            
                            // 设置任务状态
                            String status = (String) detail.get("status");
                            detailObj.setStatus(status != null ? status : "pending");
                            
                            // 设置关联的跟踪日志ID
                            Object trackLogIdObj = detail.get("assist_track_log_id");
                            if (trackLogIdObj != null) {
                                if (trackLogIdObj instanceof Long) {
                                    detailObj.setAssist_track_log_id((Long) trackLogIdObj);
                                } else if (trackLogIdObj instanceof Integer) {
                                    detailObj.setAssist_track_log_id(((Integer) trackLogIdObj).longValue());
                                }
                            }
                            
                            detailsSuggestList.add(detailObj);
                        }
                        
                        // 转换为数组
                        measuresSuggestObj.setDetails(detailsSuggestList.toArray(new com.caresync.ai.model.json.MeasuresSuggestDetails[0]));
                    }
                    
                    measuresSuggestList.add(measuresSuggestObj);
                }
                
                // 转换为数组
                vo.setMeasuresSuggest(measuresSuggestList.toArray(new com.caresync.ai.model.json.MeasuresSuggest[0]));
            }
            
        } catch (Exception e) {
            log.error("解析方案[{}]的ai_suggestions失败", scheme.getId(), e);
        }
    }

    /**
     * 计算完成进度
     */
    private void calculateCompletionRate(AiAssistScheme scheme, AssistSchemeLogVO vo) {
        try {
            // 如果方案已完成，直接设置进度为100%
            if ("COMPLETED".equals(scheme.getSchemeStatus())) {
                vo.setProgress(100);
                vo.setInProgressTasks(0);
                vo.setTotalTasks(100);
                return;
            }

            Object aiSuggestionsObj = scheme.getAiSuggestions();
            if (aiSuggestionsObj == null) {
                vo.setProgress(0);
                vo.setInProgressTasks(0);
                vo.setTotalTasks(0);
                return;
            }

            String aiSuggestions;
            if (aiSuggestionsObj instanceof String) {
                aiSuggestions = (String) aiSuggestionsObj;
            } else {
                aiSuggestions = objectMapper.writeValueAsString(aiSuggestionsObj);
            }

            // 解析JSON到Map
            Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {});

            // 计算任务完成情况
            Object measuresSuggest = aiSuggestionsMap.get("measures_suggest");
            if (measuresSuggest instanceof List) {
                List<Map<String, Object>> measuresList = (List<Map<String, Object>>) measuresSuggest;
                int totalTasks = 0;
                int completedTasks = 0;
                int inProgressTasks = 0;

                for (Map<String, Object> measure : measuresList) {
                    Object detailsObj = measure.get("details");
                    if (detailsObj instanceof List) {
                        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                        for (Map<String, Object> detail : detailsList) {
                            totalTasks++;
                            String status = (String) detail.get("status");
                            if ("completed".equals(status)) {
                                completedTasks++;
                            } else if ("in_progress".equals(status)) {
                                inProgressTasks++;
                            }
                        }
                    }
                }

                vo.setProgress(completedTasks);
                vo.setInProgressTasks(inProgressTasks);
                vo.setTotalTasks(totalTasks);
            } else {
                vo.setProgress(0);
                vo.setInProgressTasks(0);
                vo.setTotalTasks(0);
            }

        } catch (Exception e) {
            log.error("计算方案[{}]完成进度失败", scheme.getId(), e);
            vo.setProgress(0);
            vo.setInProgressTasks(0);
            vo.setTotalTasks(0);
        }
    }

}