package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.SchemeQueryDTO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AiAssistSchemeMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.ISocialWorkerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Arrays;
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
public class AiAssistSchemeServiceImpl extends ServiceImpl<AiAssistSchemeMapper, AiAssistScheme> implements IAiAssistSchemeService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IChildService childService;

    @Lazy
    @Autowired
    private ISocialWorkerService socialWorkerService;

    @Override
    public PageResult<AssistSchemeVO> getSchemeList(SchemeQueryDTO schemeQueryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<AiAssistScheme> queryWrapper = new LambdaQueryWrapper<>();
        if (schemeQueryDTO.getChildId() != null) {
            queryWrapper.eq(AiAssistScheme::getChildId, schemeQueryDTO.getChildId());
        }
        if (schemeQueryDTO.getWorkerId() != null) {
            queryWrapper.eq(AiAssistScheme::getWorkerId, schemeQueryDTO.getWorkerId());
        }
        if (schemeQueryDTO.getSchemeStatus() != null) {
            queryWrapper.eq(AiAssistScheme::getSchemeStatus, schemeQueryDTO.getSchemeStatus());
        }
        if (schemeQueryDTO.getStartDate() != null) {
            queryWrapper.ge(AiAssistScheme::getCreateTime, schemeQueryDTO.getStartDate());
        }
        if (schemeQueryDTO.getEndDate() != null) {
            queryWrapper.le(AiAssistScheme::getCreateTime, schemeQueryDTO.getEndDate());
        }
        // 按创建时间降序排序
        queryWrapper.orderByDesc(AiAssistScheme::getCreateTime);

        // 执行分页查询
        Page<AiAssistScheme> page = new Page<>(schemeQueryDTO.getPage(), schemeQueryDTO.getPageSize());
        Page<AiAssistScheme> resultPage = this.baseMapper.selectPage(page, queryWrapper);

        // 转换为VO并补充信息
        List<AssistSchemeVO> schemeVOList = convertToSchemeVOList(resultPage.getRecords());

        // 构建返回结果
        return new PageResult<>(resultPage.getTotal(), schemeVOList);
    }

    @Override
    public PageResult<AssistSchemeVO> getSchemeListManage(SchemeQueryDTO schemeQueryDTO) {
        // 这里可以实现管理视角的查询逻辑，例如增加更多筛选条件或权限控制
        // 目前暂时复用普通视角的查询逻辑
        return getSchemeList(schemeQueryDTO);
    }

    @Override
    public DetailSchemeVO getSchemeDetail(Long id) {
        // 根据ID查询方案
        AiAssistScheme scheme = this.getById(id);
        if (scheme == null) {
            return null;
        }

        // 设置方案基本信息
        DetailSchemeVO detailVO = DetailSchemeVO.builder()
                .id(scheme.getId())
                .target(scheme.getTarget())
                .createTime(scheme.getCreateTime())
                .cycle(scheme.getCycle())
                .schemeStatus(scheme.getSchemeStatus())
                .workerAdjustReason(scheme.getWorkerAdjustReason())
                .workerId(scheme.getWorkerId())
                .build();

        // 查询并设置社工姓名
        SocialWorkerInfoVO workerInfo = socialWorkerService.getSocialWorkerInfo(scheme.getWorkerId());
        if (workerInfo != null) {
            detailVO.setWorkerName(workerInfo.getName());
        }


        // 使用getById获取完整的Child对象，而不仅仅是ChildInfoVO
        Child child = childService.getById(scheme.getChildId());
        log.info("aiStructInfo: {}", child.getAiStructInfo());
        //从Child.AiStructInfo(String)
        //解析json字符串获取emotionScores字段
        if (child != null) {
            // 查询并设置儿童详细信息
            DetailSchemeVO.ChildDetailInfo childDetailInfo = DetailSchemeVO.ChildDetailInfo.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .age(child.getAge())
                        .gender(child.getGender())
                        .riskLevel(child.getRiskLevel())
                        .build();
            
            // 从aiStructInfo中提取emotion_scores和emotion_trend
            String aiStructInfoStr = (String) child.getAiStructInfo();
            log.info("aiStructInfoStr: {}", aiStructInfoStr);
            if (aiStructInfoStr != null && !aiStructInfoStr.isEmpty()) {
                try {
                    // 将 JSON 字符串解析为 Map<String, Object>
                    Map<String, Object> aiStructInfoMap = objectMapper.readValue(aiStructInfoStr, new TypeReference<Map<String, Object>>() {});
                    log.info("解析后的aiStructInfoMap: {}", aiStructInfoMap);

                    // 提取 emotion_scores
                    Object emotionScores = aiStructInfoMap.get("emotion_scores");
                    if (emotionScores instanceof Map) {
                        childDetailInfo.setEmotionScores((Map<String, Object>) emotionScores);
                        log.info("提取到的emotionScores: {}", emotionScores);
                    } else {
                        log.info("emotion_scores不是Map类型，值为: {}", emotionScores);
                    }

                    // 提取 emotion_trend
                    Object emotionTrend = aiStructInfoMap.get("emotion_trend");
                    if (emotionTrend instanceof List) {
                        childDetailInfo.setEmotionTrend((List<String>) emotionTrend);
                        log.info("提取到的emotionTrend: {}", emotionTrend);
                    } else {
                        log.info("emotion_trend不是List类型，值为: {}", emotionTrend);
                    }
                } catch (Exception e) {
                    log.error("解析 aiStructInfo 失败", e);
                }
            }

            detailVO.setChildInfo(childDetailInfo);
        }


        // 解析ai_suggestions，提取方案目标和进度
        String aiSuggestions = (String) scheme.getAiSuggestions();
        if (aiSuggestions != null && !aiSuggestions.isEmpty()) {
            try {
                // 将 JSON 字符串解析为 Map<String, Object>
                Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {});

                // 提取 target_suggest（List<String>）
                Object targetSuggest = aiSuggestionsMap.get("target_suggest");
                if (targetSuggest instanceof List) {
                    detailVO.setTargetSuggest((List<String>) targetSuggest);
                }

                // 提取 measures_suggest（List<Map>）
                Object measuresSuggest = aiSuggestionsMap.get("measures_suggest");
                if (measuresSuggest instanceof List) {
                    List<Map<String, Object>> measuresList = (List<Map<String, Object>>) measuresSuggest;
                    log.info("提取到的measuresList: {}", measuresList);
                    // 转换为 WeeklyMeasure 列表

                    List<DetailSchemeVO.WeeklyMeasure> weeklyMeasures = measuresList.stream()
                            .map(measure -> {
                                DetailSchemeVO.WeeklyMeasure weeklyMeasure = new DetailSchemeVO.WeeklyMeasure();
                                 
                                // 设置周数
                                String weekContent = (String) measure.get("week");
                                if (weekContent == null || weekContent.isEmpty()) {
                                    weekContent = (String) measure.get("content");
                                }
                                weeklyMeasure.setWeek(weekContent);
                                log.info("设置周数: {}", weekContent);
                                 
                                // 创建任务详情列表
                                List<DetailSchemeVO.TaskDetail> taskDetails = new ArrayList<>();
                                 
                                // 检查measure中是否有details字段
                                Object detailsObj = measure.get("details");
                                if (detailsObj instanceof List) {
                                    List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                                    log.info("提取到的detailsList: {}", detailsList);
                                    
                                    // 处理每个detail对象
                                    for (Map<String, Object> detail : detailsList) {
                                        DetailSchemeVO.TaskDetail taskDetail = new DetailSchemeVO.TaskDetail();
                                        
                                        // 提取content
                                        String content = (String) detail.get("content");
                                        if (content == null || content.isEmpty()) {
                                            content = "任务内容";
                                        }
                                        taskDetail.setContent(content);
                                        
                                        // 提取status
                                        String status = (String) detail.get("status");
                                        if (status == null || status.isEmpty()) {
                                            status = "pending";
                                        }
                                        taskDetail.setStatus(status);
                                        
                                        // 提取assist_track_log_id
                                        Object trackLogId = detail.get("assist_track_log_id");
                                        if (trackLogId instanceof Long) {
                                            taskDetail.setAssistTrackLogId((Long) trackLogId);
                                        } else if (trackLogId instanceof Integer) {
                                            taskDetail.setAssistTrackLogId(((Integer) trackLogId).longValue());
                                        }
                                        
                                        log.info("提取的任务详情 - content: {}, status: {}, assistTrackLogId: {}", 
                                                content, status, trackLogId);
                                        taskDetails.add(taskDetail);
                                    }
                                } else {
                                    // 如果没有details字段，创建一个默认的任务详情
                                    DetailSchemeVO.TaskDetail taskDetail = new DetailSchemeVO.TaskDetail();
                                    taskDetail.setContent("本周任务");
                                    taskDetail.setStatus("pending");
                                    taskDetails.add(taskDetail);
                                    log.info("未找到details字段，使用默认任务详情");
                                }
                                 
                                weeklyMeasure.setDetails(taskDetails);
                                log.info("创建的WeeklyMeasure: {}", weeklyMeasure);
                                return weeklyMeasure;
                            })
                            .collect(Collectors.toList());
                    detailVO.setMeasuresSuggest(weeklyMeasures);
                    log.info("最终设置的measuresSuggest: {}", weeklyMeasures);
                } else {
                    log.info("measures_suggest不是List类型，值为: {}", measuresSuggest);
                }
            } catch (Exception e) {
                log.error("解析 aiSuggestions 失败", e);
            }
        }

        return detailVO;
    }

    /**
     * 将AiAssistScheme列表转换为AssistSchemeVO列表，并补充儿童和社工姓名
     */
    private List<AssistSchemeVO> convertToSchemeVOList(List<AiAssistScheme> schemeList) {
        if (schemeList.isEmpty()) {
            return List.of();
        }

        // 提取所有儿童ID和社工ID
        List<Long> childIds = schemeList.stream()
                .map(AiAssistScheme::getChildId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> workerIds = schemeList.stream()
                .map(AiAssistScheme::getWorkerId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取儿童和社工信息
        Map<Long, String> childNameMap = childIds.stream()
                .collect(Collectors.toMap(id -> id, id -> {
                    ChildInfoVO childInfo = childService.getChildInfo(id);
                    return childInfo != null ? childInfo.getName() : "未知儿童";
                }));

        Map<Long, String> workerNameMap = workerIds.stream()
                .collect(Collectors.toMap(id -> id, id -> {
                    SocialWorkerInfoVO workerInfo = socialWorkerService.getSocialWorkerInfo(id);
                    return workerInfo != null ? workerInfo.getName() : "未知社工";
                }));

        // 转换并补充信息
        return schemeList.stream().map(scheme -> {
            AssistSchemeVO vo = new AssistSchemeVO();
            // 复制基本属性
            vo.setId(scheme.getId());
            vo.setChildId(scheme.getChildId());
            vo.setWorkerId(scheme.getWorkerId());
            vo.setTarget(scheme.getTarget());
            // 修复类型转换问题，处理String数组转为List<String>
            if (scheme.getMeasures() instanceof String[]) {
                vo.setMeasures(Arrays.asList((String[]) scheme.getMeasures()));
            } else if (scheme.getMeasures() instanceof List) {
                vo.setMeasures((List<String>) scheme.getMeasures());
            } else {
                vo.setMeasures(Collections.emptyList());
            }
            vo.setCycle(scheme.getCycle());
            vo.setSchemeStatus(scheme.getSchemeStatus());
            vo.setAiSuggestions(scheme.getAiSuggestions());
            vo.setWorkerAdjustReason(scheme.getWorkerAdjustReason());
            vo.setAiAnalysisId(scheme.getAiAnalysisId());
            vo.setEvaluationIndex(scheme.getEvaluationIndex());
            vo.setCreateTime(scheme.getCreateTime());
            vo.setUpdateTime(scheme.getUpdateTime());
            // 补充扩展字段
            vo.setChildName(childNameMap.getOrDefault(scheme.getChildId(), "未知儿童"));
            vo.setWorkerName(workerNameMap.getOrDefault(scheme.getWorkerId(), "未知社工"));
            return vo;
        }).collect(Collectors.toList());
    }
}