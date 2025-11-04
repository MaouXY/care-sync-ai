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
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.model.entity.SocialWorker;
import com.caresync.ai.mapper.SocialWorkerMapper;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IAssistTrackLogService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.ISocialWorkerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.JwtUtil;
import com.caresync.ai.utils.PasswordEncoderUtil;
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

    @Override
    public SocialWorkerHomeVO getSocialWorkerHome(Long workerId) {
        SocialWorkerHomeVO homeVO = new SocialWorkerHomeVO();
        
        // 1. 获取第一行统计数据（写死）
        HomeStatisticsDTO statistics = new HomeStatisticsDTO();
        statistics.setChildCount(15); // 绑定儿童数
        statistics.setNewChildCount(2); // 今日新增儿童数
        statistics.setPendingEmergencyCount(1); // 待处理紧急呼叫数
        statistics.setCompletedSchemeCount(8); // 已完成服务方案数
        homeVO.setStatistics(statistics);
        
        // 2. 查询与该社工绑定的所有儿童
        LambdaQueryWrapper<Child> childQueryWrapper = new LambdaQueryWrapper<>();
        childQueryWrapper.eq(Child::getSocialWorkerId, workerId);
        List<Child> childList = childService.list(childQueryWrapper);
        
        // 3. 计算情感趋势分析（所有儿童的emotion_scores平均值）
        Map<String, Double> emotionScoresAverage = calculateEmotionScoresAverage(childList);
        homeVO.setEmotionScoresAverage(emotionScoresAverage);
        
        // 4. 计算儿童情况分布（potential_problems）
        Map<String, Integer> potentialProblemsDistribution = calculatePotentialProblemsDistribution(childList);
        homeVO.setPotentialProblemsDistribution(potentialProblemsDistribution);
        
        // 5. 查询待处理任务（进行中的服务方案）
        List<PendingTaskVO> pendingTasks = getPendingTasks(workerId);
        homeVO.setPendingTasks(pendingTasks);
        
        // 6. 查询近期活动（最近完成的子任务）
        List<RecentActivityVO> recentActivities = getRecentActivities(workerId);
        homeVO.setRecentActivities(recentActivities);
        
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
}