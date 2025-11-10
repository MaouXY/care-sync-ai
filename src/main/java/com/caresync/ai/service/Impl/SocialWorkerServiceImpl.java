package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.CodeConstant;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.constant.MessageConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.exception.BusinessException;
import com.caresync.ai.model.DTO.HomeStatisticsDTO;
import com.caresync.ai.model.DTO.SocialWorkerLoginDTO;
import com.caresync.ai.model.DTO.UpdateSocialWorkerInfoDTO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.VO.PendingTaskVO;
import com.caresync.ai.model.VO.RecentActivityVO;
import com.caresync.ai.model.VO.SocialWorkerHomeVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.entity.*;
import com.caresync.ai.mapper.SocialWorkerMapper;
import com.caresync.ai.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.JwtUtil;
import com.caresync.ai.utils.PasswordEncoderUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  社工服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Service
public class SocialWorkerServiceImpl extends ServiceImpl<SocialWorkerMapper, SocialWorker> implements ISocialWorkerService {

    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IChildService childService;

    @Autowired
    private IAiAssistSchemeService aiAssistSchemeService;

    @Autowired
    private IAssistTrackLogService assistTrackLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public LoginVO login(SocialWorkerLoginDTO socialWorkerLoginDTO) {
        String workerNo = socialWorkerLoginDTO.getWorkerNo();
        String password = socialWorkerLoginDTO.getPassword();

        // 查询社工信息
        LambdaQueryWrapper<SocialWorker> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SocialWorker::getWorkerNo, workerNo);
        SocialWorker socialWorker = this.getOne(queryWrapper);

        // 验证社工是否存在以及密码是否正确
        if (socialWorker == null || !passwordEncoderUtil.matches(password, socialWorker.getPassword())) {
            throw new BusinessException(CodeConstant.INVALID_CREDENTIALS_CODE, MessageConstant.PASSWORD_ERROR);
        }

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, socialWorker.getId());
        claims.put(JwtClaimsConstant.USER_NAME, socialWorker.getName());
        claims.put(JwtClaimsConstant.USER_ROLE, 2); // 2表示社工角色
        String token = jwtUtil.createJWT(claims);

        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setId(socialWorker.getId());
        loginVO.setName(socialWorker.getName());
        loginVO.setToken(token);
        loginVO.setRole(2);

        return loginVO;
    }

    @Override
    public SocialWorkerInfoVO getSocialWorkerInfo(Long id) {
        SocialWorker socialWorker = this.getById(id);
        if (socialWorker == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"社工不存在");
        }

        SocialWorkerInfoVO socialWorkerInfoVO = new SocialWorkerInfoVO();
        BeanUtils.copyProperties(socialWorker, socialWorkerInfoVO);
        return socialWorkerInfoVO;
    }

    @Override
    public void updateSocialWorkerInfo(UpdateSocialWorkerInfoDTO updateSocialWorkerInfoDTO) {
        SocialWorker socialWorker = new SocialWorker();
        BeanUtils.copyProperties(updateSocialWorkerInfoDTO, socialWorker);

        boolean result = this.updateById(socialWorker);
        if (!result) {
            throw new BusinessException(CodeConstant.FAIL_CODE,"更新社工信息失败");
        }
    }

    @Override
    public void logout() {
        // 清除ThreadLocal中的用户信息
        BaseContext.clear();
    }

    /**
     * 获取社工首页情感图表数据
     */
    @Override
    public List<Map<String, Object>> getChildrenEmotionChartData(Long workerId) {
        // 1. 查询与该社工绑定的所有儿童
        LambdaQueryWrapper<Child> childQueryWrapper = new LambdaQueryWrapper<>();
        childQueryWrapper.eq(Child::getSocialWorkerId, workerId);
        List<Child> childList = childService.list(childQueryWrapper);

        // 2. 处理每个儿童的情感数据
        List<Map<String, Object>> chartSeries = new ArrayList<>();

        for (Child child : childList) {
            try {
                Map<String, Object> seriesItem = new HashMap<>();
                seriesItem.put("type", "bar");
                seriesItem.put("name", child.getName() + "(" + child.getAge() + "岁)");

                // 获取该儿童的情感数据
                List<Double> emotionAverages = calculateChildEmotionAverages(child);
                seriesItem.put("data", emotionAverages);

                chartSeries.add(seriesItem);
            } catch (Exception e) {
                log.error("处理儿童{}的情感数据失败: {}",e);
            }
        }

        return chartSeries;
    }

    /**
     * 计算单个儿童的情感数据平均值
     */
    private List<Double> calculateChildEmotionAverages(Child child) {
        List<Double> emotionAverages = new ArrayList<>();

        // 定义情感指标顺序
        String[] emotionTypes = {"情绪稳定性", "焦虑水平", "幸福感", "社交自信"};

        try {
            if (child.getAiStructInfo() != null) {
                String aiStructInfoStr = child.getAiStructInfo().toString();
                JsonNode aiStructInfo = objectMapper.readTree(aiStructInfoStr);

                if (aiStructInfo.has("emotion_history")) {
                    JsonNode emotionHistory = aiStructInfo.get("emotion_history");

                    // 为每个情感指标计算平均值
                    for (String emotionType : emotionTypes) {
                        double sum = 0.0;
                        int count = 0;

                        // 遍历历史记录计算该指标的平均值
                        for (JsonNode historyItem : emotionHistory) {
                            if (historyItem.has("scores") && historyItem.get("scores").has(emotionType)) {
                                double score = historyItem.get("scores").get(emotionType).asDouble();
                                sum += score;
                                count++;
                            }
                        }

                        // 计算平均值，如果没有数据则设为0
                        double average = count > 0 ? Math.round((sum / count) * 100) / 100.0 : 0.0;
                        emotionAverages.add(average);
                    }
                } else if (aiStructInfo.has("emotion_scores")) {
                    // 如果没有历史记录，使用当前的情感评分
                    JsonNode emotionScores = aiStructInfo.get("emotion_scores");
                    for (String emotionType : emotionTypes) {
                        double score = emotionScores.has(emotionType) ?
                                emotionScores.get(emotionType).asDouble() : 0.0;
                        emotionAverages.add(score);
                    }
                } else {
                    // 如果没有情感数据，全部设为0
                    for (int i = 0; i < emotionTypes.length; i++) {
                        emotionAverages.add(0.0);
                    }
                }
            } else {
                // 如果没有AI结构化信息，全部设为0
                for (int i = 0; i < emotionTypes.length; i++) {
                    emotionAverages.add(0.0);
                }
            }
        } catch (Exception e) {
            log.error("解析儿童{}的情感数据失败: {}",e);
            // 发生错误时全部设为0
            for (int i = 0; i < emotionTypes.length; i++) {
                emotionAverages.add(0.0);
            }
        }

        return emotionAverages;
    }

    /**
     * 获取社工首页情感图表数据
     */
    @Override
    public SocialWorkerHomeVO getSocialWorkerHome(Long workerId) {
        SocialWorkerHomeVO homeVO = new SocialWorkerHomeVO();

        // 1. 获取第一行统计数据
        HomeStatisticsDTO statistics = new HomeStatisticsDTO();
        // 绑定儿童数
        statistics.setChildCount((int) childService.count(new LambdaQueryWrapper<Child>().eq(Child::getSocialWorkerId, workerId)));
        //状态为进行中的服务方案数
        statistics.setPendingTaskCount((int) aiAssistSchemeService.count(new LambdaQueryWrapper<AiAssistScheme>()
                .eq(AiAssistScheme::getWorkerId, workerId)
                .eq(AiAssistScheme::getSchemeStatus, "IN_PROGRESS")));// TODO 写成静态常量
        // 已完成服务方案数
        statistics.setCompletedSchemeCount((int) aiAssistSchemeService.count(new LambdaQueryWrapper<AiAssistScheme>()
                .eq(AiAssistScheme::getWorkerId, workerId)
                .eq(AiAssistScheme::getSchemeStatus, "COMPLETED")));// TODO 写成静态常量
        // AI分析结果数
        statistics.setAiAnalysisCount(statistics.getChildCount());

        homeVO.setStatistics(statistics);

        // 2. 查询与该社工绑定的所有儿童
        LambdaQueryWrapper<Child> childQueryWrapper = new LambdaQueryWrapper<>();
        childQueryWrapper.eq(Child::getSocialWorkerId, workerId);
        List<Child> childList = childService.list(childQueryWrapper);

//        // 3. 计算情感趋势分析（所有儿童的emotion_scores平均值）
//        Map<String, Double> emotionScoresAverage = calculateEmotionScoresAverage(childList);
//        homeVO.setEmotionScoresAverage(emotionScoresAverage);

//        // 4. 计算儿童情况分布（potential_problems）
//        Map<String, Integer> potentialProblemsDistribution = calculatePotentialProblemsDistribution(childList);
//        homeVO.setPotentialProblemsDistribution(potentialProblemsDistribution);

        // 5. 查询待处理任务（进行中的服务方案）
        List<PendingTaskVO> pendingTasks = getPendingTasks(workerId);
        homeVO.setPendingTasks(pendingTasks);

        // 6. 查询近期活动（最近完成的子任务）
        List<RecentActivityVO> recentActivities = getRecentActivities(workerId);
        homeVO.setRecentActivities(recentActivities);
        
        // 7. 计算儿童情感数据图表数据
        Map<String, Object> emotionChartResult = calculateEmotionChartData(childList);
        homeVO.setEmotionChartData((List<Map<String, Object>>) emotionChartResult.get("series"));
        homeVO.setEmotionChartTimeAxis((List<String>) emotionChartResult.get("timeAxis"));
        
        // 8. 计算雷达图数据（情感评分占比）
        Map<String, Double> radarChartData = calculateRadarChartData(childList);
        homeVO.setRadarChartData(radarChartData);

        return homeVO;
    }

    /**
     * 计算所有儿童的情感评分平均值
     */
    private Map<String, Double> calculateEmotionScoresAverage(List<Child> childList) {
        Map<String, Double> emotionScoresSum = new HashMap<>();
        Map<String, Integer> emotionScoresCount = new HashMap<>();

        for (Child child : childList) {
            try {
                if (child.getAiStructInfo() instanceof Map) {
                    Map<String, Object> aiStructInfo = (Map<String, Object>) child.getAiStructInfo();
                    if (aiStructInfo.containsKey("emotion_scores") && aiStructInfo.get("emotion_scores") instanceof Map) {
                        Map<String, Object> emotionScores = (Map<String, Object>) aiStructInfo.get("emotion_scores");

                        for (Map.Entry<String, Object> entry : emotionScores.entrySet()) {
                            String emotionType = entry.getKey();
                            Double score = entry.getValue() instanceof Number ?
                                    ((Number) entry.getValue()).doubleValue() : 0.0;

                            emotionScoresSum.put(emotionType, emotionScoresSum.getOrDefault(emotionType, 0.0) + score);
                            emotionScoresCount.put(emotionType, emotionScoresCount.getOrDefault(emotionType, 0) + 1);
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 计算平均值
        Map<String, Double> emotionScoresAverage = new HashMap<>();
        for (Map.Entry<String, Double> entry : emotionScoresSum.entrySet()) {
            String emotionType = entry.getKey();
            Double sum = entry.getValue();
            Integer count = emotionScoresCount.getOrDefault(emotionType, 1);
            emotionScoresAverage.put(emotionType, Math.round(sum / count * 100) / 100.0);
        }

        return emotionScoresAverage;
    }

    /**
     * 计算儿童潜在问题分布
     */
    private Map<String, Integer> calculatePotentialProblemsDistribution(List<Child> childList) {
        Map<String, Integer> distribution = new HashMap<>();

        for (Child child : childList) {
            try {
                if (child.getAiStructInfo() instanceof Map) {
                    Map<String, Object> aiStructInfo = (Map<String, Object>) child.getAiStructInfo();
                    if (aiStructInfo.containsKey("potential_problems")) {
                        Object potentialProblems = aiStructInfo.get("potential_problems");
                        if (potentialProblems instanceof String) {
                            String problem = (String) potentialProblems;
                            distribution.put(problem, distribution.getOrDefault(problem, 0) + 1);
                        } else if (potentialProblems instanceof List) {
                            List<String> problems = (List<String>) potentialProblems;
                            for (String problem : problems) {
                                distribution.put(problem, distribution.getOrDefault(problem, 0) + 1);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        return distribution;
    }

    /**
     * 获取待处理任务（进行中的服务方案）
     */
    private List<PendingTaskVO> getPendingTasks(Long workerId) {
        LambdaQueryWrapper<AiAssistScheme> schemeQueryWrapper = new LambdaQueryWrapper<>();
        schemeQueryWrapper.eq(AiAssistScheme::getWorkerId, workerId)
                .eq(AiAssistScheme::getSchemeStatus, "IN_PROGRESS")
                .orderByDesc(AiAssistScheme::getUpdateTime);
        List<AiAssistScheme> schemeList = aiAssistSchemeService.list(schemeQueryWrapper);

        // 转换为PendingTaskVO
        List<PendingTaskVO> pendingTasks = new ArrayList<>();
        for (AiAssistScheme scheme : schemeList) {
            PendingTaskVO taskVO = new PendingTaskVO();
            taskVO.setId(scheme.getId());
            taskVO.setChildId(scheme.getChildId());
            taskVO.setTarget(scheme.getTarget());

            // 计算结束时间（假设从创建时间开始，加上周期天数）
            LocalDateTime endTime = scheme.getCreateTime().plusDays(scheme.getCycle() != null ? scheme.getCycle() : 7);
            taskVO.setEndTime(endTime);

            // 计算剩余天数
            long remainingDays = LocalDateTime.now().until(endTime, ChronoUnit.DAYS);
            taskVO.setRemainingDays(Math.max(0, (int) remainingDays));

            // 获取儿童姓名
            Child child = childService.getById(scheme.getChildId());
            if (child != null) {
                taskVO.setChildName(child.getName());
            }

            pendingTasks.add(taskVO);
        }

        return pendingTasks;
    }

    /**
     * 获取近期活动（最近完成的子任务）
     */
    private List<RecentActivityVO> getRecentActivities(Long workerId) {
        LambdaQueryWrapper<AssistTrackLog> logQueryWrapper = new LambdaQueryWrapper<>();
        logQueryWrapper.eq(AssistTrackLog::getWorkerId, workerId)
                .eq(AssistTrackLog::getCompletionStatus, "completed")
                .orderByDesc(AssistTrackLog::getCreateTime)
                .last("LIMIT 5"); // 限制只返回最近5条
        List<AssistTrackLog> logList = assistTrackLogService.list(logQueryWrapper);

        // 转换为RecentActivityVO
        List<RecentActivityVO> recentActivities = new ArrayList<>();
        for (AssistTrackLog log : logList) {
            RecentActivityVO activityVO = new RecentActivityVO();
            BeanUtils.copyProperties(log, activityVO);

            // 获取儿童姓名
            Child child = childService.getById(log.getChildId());
            if (child != null) {
                activityVO.setChildName(child.getName());
            }

            recentActivities.add(activityVO);
        }

        return recentActivities;
    }

    /**
     * 计算儿童情感数据图表数据（时间序列）
     */
    private Map<String, Object> calculateEmotionChartData(List<Child> childList) {
        Map<String, Object> chartResult = new HashMap<>();
        List<Map<String, Object>> chartSeries = new ArrayList<>();
        
        // 定义情感指标
        String[] emotionTypes = {"情绪稳定性", "焦虑水平", "幸福感", "社交自信"};
        
        // 收集所有时间点的数据
        Map<String, Map<String, List<Double>>> timeEmotionData = new HashMap<>();
        
        for (Child child : childList) {
            try {
                if (child.getAiStructInfo() != null) {
                    String aiStructInfoStr = child.getAiStructInfo().toString();
                    JsonNode aiStructInfo = objectMapper.readTree(aiStructInfoStr);
                    
                    if (aiStructInfo.has("emotion_history")) {
                        JsonNode emotionHistory = aiStructInfo.get("emotion_history");
                        
                        for (JsonNode historyItem : emotionHistory) {
                            if (historyItem.has("date") && historyItem.has("scores")) {
                                String date = historyItem.get("date").asText();
                                JsonNode scores = historyItem.get("scores");
                                
                                // 初始化该时间点的数据结构
                                if (!timeEmotionData.containsKey(date)) {
                                    timeEmotionData.put(date, new HashMap<>());
                                    for (String emotionType : emotionTypes) {
                                        timeEmotionData.get(date).put(emotionType, new ArrayList<>());
                                    }
                                }
                                
                                // 添加该儿童在该时间点的分数
                                for (String emotionType : emotionTypes) {
                                    if (scores.has(emotionType)) {
                                        double score = scores.get(emotionType).asDouble();
                                        timeEmotionData.get(date).get(emotionType).add(score);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析儿童{}的情感数据失败: {}",e);
            }
        }
        
        // 按时间排序
        List<String> sortedDates = timeEmotionData.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        
        // 为每种情感指标创建时间序列数据
        for (String emotionType : emotionTypes) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("type", "line");
            seriesItem.put("name", emotionType);
            
            List<Double> emotionData = new ArrayList<>();
            
            for (String date : sortedDates) {
                List<Double> scores = timeEmotionData.get(date).get(emotionType);
                if (!scores.isEmpty()) {
                    // 计算该时间点所有儿童的平均值
                    double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
                    double average = Math.round((sum / scores.size()) * 100) / 100.0;
                    emotionData.add(average);
                } else {
                    // 如果没有数据，设为0
                    emotionData.add(0.0);
                }
            }
            
            seriesItem.put("data", emotionData);
            chartSeries.add(seriesItem);
        }
        
        chartResult.put("series", chartSeries);
        chartResult.put("timeAxis", sortedDates);
        
        return chartResult;
    }
    
    /**
     * 计算雷达图数据（情感评分占比）
     * 计算所有儿童在四种情感属性上的总分占比
     */
    private Map<String, Double> calculateRadarChartData(List<Child> childList) {
        Map<String, Double> radarData = new HashMap<>();
        
        // 定义情感指标
        String[] emotionTypes = {"情绪稳定性", "焦虑水平", "幸福感", "社交自信"};
        
        // 初始化总分和计数
        Map<String, Double> emotionScoresSum = new HashMap<>();
        Map<String, Integer> emotionScoresCount = new HashMap<>();
        
        for (String emotionType : emotionTypes) {
            emotionScoresSum.put(emotionType, 0.0);
            emotionScoresCount.put(emotionType, 0);
        }
        
        // 计算每个情感指标的总分和计数
        for (Child child : childList) {
            try {
                if (child.getAiStructInfo() != null) {
                    String aiStructInfoStr = child.getAiStructInfo().toString();
                    JsonNode aiStructInfo = objectMapper.readTree(aiStructInfoStr);
                    
                    // 优先使用emotion_scores，如果没有则使用emotion_history的最新记录
                    if (aiStructInfo.has("emotion_scores")) {
                        JsonNode emotionScores = aiStructInfo.get("emotion_scores");
                        for (String emotionType : emotionTypes) {
                            if (emotionScores.has(emotionType)) {
                                double score = emotionScores.get(emotionType).asDouble();
                                emotionScoresSum.put(emotionType, emotionScoresSum.get(emotionType) + score);
                                emotionScoresCount.put(emotionType, emotionScoresCount.get(emotionType) + 1);
                            }
                        }
                    } else if (aiStructInfo.has("emotion_history")) {
                        JsonNode emotionHistory = aiStructInfo.get("emotion_history");
                        if (emotionHistory.size() > 0) {
                            // 使用最新的历史记录
                            JsonNode latestRecord = emotionHistory.get(emotionHistory.size() - 1);
                            if (latestRecord.has("scores")) {
                                JsonNode scores = latestRecord.get("scores");
                                for (String emotionType : emotionTypes) {
                                    if (scores.has(emotionType)) {
                                        double score = scores.get(emotionType).asDouble();
                                        emotionScoresSum.put(emotionType, emotionScoresSum.get(emotionType) + score);
                                        emotionScoresCount.put(emotionType, emotionScoresCount.get(emotionType) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析儿童{}的情感数据失败: {}",e);
            }
        }
        
        // 计算每个情感指标的平均值
        Map<String, Double> emotionAverages = new HashMap<>();
        double totalAverage = 0.0;
        int validEmotionCount = 0;
        
        for (String emotionType : emotionTypes) {
            int count = emotionScoresCount.get(emotionType);
            if (count > 0) {
                double average = emotionScoresSum.get(emotionType) / count;
                emotionAverages.put(emotionType, Math.round(average * 100) / 100.0);
                totalAverage += average;
                validEmotionCount++;
            } else {
                emotionAverages.put(emotionType, 0.0);
            }
        }
        
        // 计算占比（每个情感指标的平均值占总平均值的比例）
        if (validEmotionCount > 0) {
            double overallAverage = totalAverage / validEmotionCount;
            
            for (String emotionType : emotionTypes) {
                double average = emotionAverages.get(emotionType);
                if (overallAverage > 0) {
                    // 计算占比，并转换为百分比（0-100）
                    double percentage = (average / overallAverage) * 100;
                    radarData.put(emotionType, Math.round(percentage * 100) / 100.0);
                } else {
                    radarData.put(emotionType, 0.0);
                }
            }
        } else {
            // 如果没有有效数据，全部设为100%
            for (String emotionType : emotionTypes) {
                radarData.put(emotionType, 100.0);
            }
        }
        
        return radarData;
    }
}