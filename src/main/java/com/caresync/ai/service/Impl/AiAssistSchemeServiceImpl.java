package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.GenerateSchemeDTO;
import com.caresync.ai.model.DTO.SchemeQueryDTO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.AiAnalysisLog;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AiAssistSchemeMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IAiAnalysisLogService;
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
import java.util.Collections;

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

    @Autowired
    private IAiAnalysisLogService aiAnalysisLogService;

    /**
     * 获取辅助方案列表
     * @param schemeQueryDTO 查询条件，包含儿童ID、社工ID、方案状态、开始时间、结束时间等查询参数（均允许空值）
     * @return 分页结果
     */
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

    // 未实现
    @Override
    public PageResult<AssistSchemeVO> getSchemeListManage(SchemeQueryDTO schemeQueryDTO) {
        // 这里可以实现管理视角的查询逻辑，例如增加更多筛选条件或权限控制
        // 目前暂时复用普通视角的查询逻辑
        return getSchemeList(schemeQueryDTO);
    }

    /**
     * 获取辅助方案详情
     * @param id 方案ID
     * @return 方案详情VO
     */
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
     * 生成辅助方案
     * @param generateSchemeDTO 生成方案DTO，包含儿童ID、社工ID、目标、周期、额外信息等
     * @return 辅助方案VO
     */
    @Override
    public AssistSchemeVO generateScheme(GenerateSchemeDTO generateSchemeDTO) {
        log.info("开始生成AI服务方案，儿童ID: {}", generateSchemeDTO.getChildId());
        
        // 1. 获取儿童信息
        Child child = childService.getById(generateSchemeDTO.getChildId());
        if (child == null) {
            log.error("未找到ID为{}的儿童信息", generateSchemeDTO.getChildId());
            throw new RuntimeException("儿童信息不存在");
        }
        
        // 2. 获取最新的AI分析记录
        AiAnalysisLog latestAnalysis = getLatestAiAnalysisLog(child.getId());
        if (latestAnalysis == null) {
            log.error("儿童ID: {} 没有AI分析记录", child.getId());
            throw new RuntimeException("该儿童暂无AI分析记录，请先进行AI分析");
        }
        
        // 3. 解析AI分析结果
        String analysisResult = parseAnalysisResult(latestAnalysis.getAnalysisResult());
        
        // 4. 生成AI建议（这里需要调用AI服务，暂时使用模拟数据）
        String aiSuggestions = generateAiSuggestions(analysisResult, generateSchemeDTO.getAdditionalInfo());
        
        // 5. 创建服务方案
        AiAssistScheme scheme = new AiAssistScheme();
        scheme.setChildId(child.getId());
        scheme.setWorkerId(1L); // 默认社工ID，实际应该从登录信息获取
        scheme.setTarget(generateSchemeDTO.getAdditionalInfo() != null ? 
                        generateSchemeDTO.getAdditionalInfo() : "缓解孤独感，提升社交能力");
        scheme.setMeasures(new String[]{"建立信任关系", "情绪识别与表达", "社交技能培养", "总结与展望"});
        scheme.setCycle(7); // 默认1周
        scheme.setSchemeStatus("DRAFT");
        scheme.setAiSuggestions(aiSuggestions);
        scheme.setAiAnalysisId(latestAnalysis.getId());
        
        // 6. 保存服务方案
        boolean saveSuccess = this.save(scheme);
        if (!saveSuccess) {
            log.error("保存服务方案失败，儿童ID: {}", child.getId());
            throw new RuntimeException("生成服务方案失败");
        }
        
        log.info("成功生成服务方案，方案ID: {}", scheme.getId());
        
        // 7. 转换为VO并返回
        AssistSchemeVO vo = new AssistSchemeVO();
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
        vo.setAiAnalysisId(scheme.getAiAnalysisId());
        vo.setCreateTime(scheme.getCreateTime());
        
        // 补充儿童和社工姓名
        ChildInfoVO childInfo = childService.getChildInfo(child.getId());
        if (childInfo != null) {
            vo.setChildName(childInfo.getName());
        }
        
        SocialWorkerInfoVO workerInfo = socialWorkerService.getSocialWorkerInfo(scheme.getWorkerId());
        if (workerInfo != null) {
            vo.setWorkerName(workerInfo.getName());
        }
        
        return vo;
    }
    
    /**
     * 获取最新的AI分析记录
     */
    private AiAnalysisLog getLatestAiAnalysisLog(Long childId) {
        // 使用LambdaQueryWrapper查询最新的AI分析记录
        LambdaQueryWrapper<AiAnalysisLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiAnalysisLog::getChildId, childId)
                   .orderByDesc(AiAnalysisLog::getCreateTime)
                   .last("LIMIT 1");
        
        return aiAnalysisLogService.getOne(queryWrapper);
    }
    
    /**
     * 解析AI分析结果
     */
    private String parseAnalysisResult(Object analysisResult) {
        try {
            if (analysisResult == null) {
                return "暂无分析结果";
            }
            
            if (analysisResult instanceof String) {
                return (String) analysisResult;
            } else {
                // 如果是JSON对象，转换为字符串
                return objectMapper.writeValueAsString(analysisResult);
            }
        } catch (Exception e) {
            log.error("解析AI分析结果失败", e);
            return "解析分析结果失败";
        }
    }
    
    /**
     * 生成AI建议（模拟实现，实际应该调用AI服务）
     */
    private String generateAiSuggestions(String analysisResult, String additionalInfo) {
        try {
            // 模拟AI建议生成
            Map<String, Object> aiSuggestions = new HashMap<>();
            
            // 目标建议
            List<String> targetSuggest = Arrays.asList(
                "降低孤独焦虑，建立积极心态",
                "增强情绪管理，正确表达感受", 
                "提升社交能力，改善人际沟通"
            );
            aiSuggestions.put("target_suggest", targetSuggest);
            
            // 服务措施建议
            List<Map<String, Object>> measuresSuggest = new ArrayList<>();
            
            // 第1周：建立信任关系
            Map<String, Object> week1 = new HashMap<>();
            week1.put("week", "建立信任关系");
            week1.put("details", Arrays.asList(
                createTaskDetail("初次见面，了解小明的兴趣爱好和日常生活情况。", "pending"),
                createTaskDetail("一起参与小明感兴趣的活动（如绘画、下棋），建立初步信任。", "pending"),
                createTaskDetail("与小明约定每周固定的见面时间，增加安全感。", "pending")
            ));
            measuresSuggest.add(week1);
            
            // 第2周：情绪识别与表达
            Map<String, Object> week2 = new HashMap<>();
            week2.put("week", "情绪识别与表达");
            week2.put("details", Arrays.asList(
                createTaskDetail("通过情绪卡片游戏，帮助小明识别不同的情绪。", "pending"),
                createTaskDetail("引导小明用绘画的方式表达自己的内心感受。", "pending"),
                createTaskDetail("教授简单的情绪调节方法，如深呼吸、倾诉等。", "pending")
            ));
            measuresSuggest.add(week2);
            
            // 第3周：社交技能培养
            Map<String, Object> week3 = new HashMap<>();
            week3.put("week", "社交技能培养");
            week3.put("details", Arrays.asList(
                createTaskDetail("组织小组活动，鼓励小明与其他小朋友互动。", "pending"),
                createTaskDetail("角色扮演练习，学习如何与他人友好沟通。", "pending"),
                createTaskDetail("分享正面社交经验，增强小明的自信心。", "pending")
            ));
            measuresSuggest.add(week3);
            
            // 第4周：总结与展望
            Map<String, Object> week4 = new HashMap<>();
            week4.put("week", "总结与展望");
            week4.put("details", Arrays.asList(
                createTaskDetail("回顾四周的变化，肯定小明的进步。", "pending"),
                createTaskDetail("共同制定后续计划，帮助小明保持积极状态。", "pending"),
                createTaskDetail("与家长沟通，分享小明的成长和需要继续关注的方面。", "pending")
            ));
            measuresSuggest.add(week4);
            
            aiSuggestions.put("measures_suggest", measuresSuggest);
            
            return objectMapper.writeValueAsString(aiSuggestions);
        } catch (Exception e) {
            log.error("生成AI建议失败", e);
            return "{}";
        }
    }
    
    /**
     * 创建任务详情
     */
    private Map<String, Object> createTaskDetail(String content, String status) {
        Map<String, Object> taskDetail = new HashMap<>();
        taskDetail.put("content", content);
        taskDetail.put("status", status);
        // assist_track_log_id在保存时添加
        return taskDetail;
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