package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.CodeConstant;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.constant.MessageConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.exception.BusinessException;
import com.caresync.ai.model.DTO.ChildLoginDTO;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.UpdateChildInfoDTO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.ChildVO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.ChildMapper;
import com.caresync.ai.model.entity.SocialWorker;
import com.caresync.ai.model.json.AiStructInfo;
import com.caresync.ai.model.json.EmotionScores;
import com.caresync.ai.model.json.Recommendations;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IChildService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.service.ISocialWorkerService;
import com.caresync.ai.utils.JwtUtil;
import com.caresync.ai.utils.PasswordEncoderUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  儿童服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Slf4j
@Service
public class ChildServiceImpl extends ServiceImpl<ChildMapper, Child> implements IChildService {

    @Autowired
    private JwtConfig jwtConfig;

    @Lazy
    @Autowired
    private ISocialWorkerService socialWorkerService;

    @Autowired
    private ObjectMapper objectMapper;

    /*####################儿童端####################*/
    /**
     * 儿童登录
     * @param childLoginDTO 儿童登录参数
     * @return 登录响应结果
     */
    @Override
    public LoginVO login(ChildLoginDTO childLoginDTO) {
        String childNo = childLoginDTO.getChildNo();
        String verifyCode = PasswordEncoderUtil.encode(childLoginDTO.getVerifyCode());

        // 查询儿童信息
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Child::getChildNo, childNo);
        Child child = this.getOne(queryWrapper);

        log.info("输入密码: {}", verifyCode);
        log.info("数据库密码: {}", child.getVerifyCode());

        // 验证儿童是否存在以及验证码是否正确
        if (child == null || !verifyCode.equals(child.getVerifyCode())) {
            throw new BusinessException(CodeConstant.INVALID_CREDENTIALS_CODE,MessageConstant.PASSWORD_ERROR);
        }

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, child.getId());
        claims.put(JwtClaimsConstant.USER_NAME, child.getName());
        claims.put(JwtClaimsConstant.USER_ROLE, 1); // 1表示儿童角色
        String token = JwtUtil.createJWT(jwtConfig.getSecret(), jwtConfig.getExpiration() * 1000, claims);

        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setId(child.getId());
        loginVO.setName(child.getName());
        loginVO.setToken(token);
        loginVO.setRole(1);

        return loginVO;
    }

    /**
     * 获取儿童信息-儿童端
     * @param id 儿童ID
     * @return 儿童信息VO
     */
    @Override
    public ChildVO getChild(Long id) {
        Child child = this.getById(id);
        if (child == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"儿童不存在");
        }

        ChildVO childVO = new ChildVO();
        BeanUtils.copyProperties(child, childVO);

        // 关联查询社工信息
        if (child.getSocialWorkerId() != null) {
            SocialWorker socialWorker = socialWorkerService.getById(child.getSocialWorkerId());
            if (socialWorker != null) {
                childVO.setSocialWorkerName(socialWorker.getName());
                childVO.setSocialWorkerPhone(socialWorker.getPhone());
            }
        }

        return childVO;
    }

    /**
     * 获取儿童信息
     * @param id 儿童ID
     * @return 儿童信息VO
     */
    @Override
    public ChildInfoVO getChildInfo(Long id) {
        Child child = this.getById(id);
        if (child == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"儿童不存在");
        }

        ChildInfoVO childInfoVO = ChildInfoVO.builder()
                .id(child.getId())
                .childNo(child.getChildNo())
                .serviceStatus(child.getServiceStatus())
                .riskLevel(child.getRiskLevel())
                .name(child.getName())
                .age(child.getAge())
                .gender(child.getGender())
                .birthDate(child.getBirthDate().atStartOfDay())
                .idCard(child.getIdCard())
                .address(child.getAddress())
                .notes(child.getNotes())
                .phone(child.getPhone())
                .guardianName(child.getGuardianName())
                .guardianPhone(child.getGuardianPhone())
                .hasNewChat(child.getHasNewChat())
                .aiStructInfo(parseAiAnalysisToAiStructInfo(child.getAiStructInfo()))
                .aiAnalysisTime(child.getAiAnalysisTime())
                .createTime(child.getCreateTime())
                .updateTime(child.getUpdateTime())
                .build();
        return childInfoVO;
    }

    /**
     * 将AI分析结果解析为AiStructInfo对象
     */
    private AiStructInfo parseAiAnalysisToAiStructInfo(Object aiAnalysisObj) {
        try {
            if (aiAnalysisObj == null) {
                return null;
            }

            String aiAnalysis;
            if (aiAnalysisObj instanceof String) {
                aiAnalysis = (String) aiAnalysisObj;
            } else {
                // 如果不是String类型，转换为JSON字符串
                aiAnalysis = objectMapper.writeValueAsString(aiAnalysisObj);
            }

            // 解析JSON到Map
            Map<String, Object> aiAnalysisMap = objectMapper.readValue(aiAnalysis, new TypeReference<Map<String, Object>>() {});

            // 创建AiStructInfo对象
            AiStructInfo aiStructInfo = new AiStructInfo();

            // 提取emotion_trend
            Object emotionTrend = aiAnalysisMap.get("emotion_trend");
            if (emotionTrend instanceof List) {
                aiStructInfo.setEmotionTrend((String[]) emotionTrend);
            }

            // 提取core_needs
            Object coreNeeds = aiAnalysisMap.get("core_needs");
            if (coreNeeds instanceof List) {
                aiStructInfo.setCoreNeeds((String[]) coreNeeds);
            }

            // 提取potential_problems
            Object potentialProblems = aiAnalysisMap.get("potential_problems");
            if (potentialProblems instanceof String) {
                aiStructInfo.setPotentialProblems((String) potentialProblems);
            }

            // 提取description
            Object description = aiAnalysisMap.get("description");
            if (description instanceof String) {
                aiStructInfo.setDescription((String) description);
            }

            // 提取latest_analysis
            Object latestAnalysis = aiAnalysisMap.get("latest_analysis");
            if (latestAnalysis instanceof String) {
                aiStructInfo.setLatestAnalysis(LocalDateTime.parse((String) latestAnalysis));
            }

            // 提取emotion_scores
            Object emotionScores = aiAnalysisMap.get("emotion_scores");
            if (emotionScores instanceof Map) {
                Map<String, Object> scoresMap = (Map<String, Object>) emotionScores;
                EmotionScores emotionScoresObj = new EmotionScores();

                // 设置情绪分数
                if (scoresMap.get("stability") instanceof Number) {//情绪稳定性
                    emotionScoresObj.setStability(((Number) scoresMap.get("stability")).intValue());
                }
                if (scoresMap.get("anxiety") instanceof Number) {//焦虑水平
                    emotionScoresObj.setAnxiety(((Number) scoresMap.get("anxiety")).intValue());
                }
                if (scoresMap.get("happiness") instanceof Number) {//幸福感
                    emotionScoresObj.setHappiness(((Number) scoresMap.get("happiness")).intValue());
                }
                if (scoresMap.get("socialConfidence") instanceof Number) {//社交自信
                    emotionScoresObj.setSocialConfidence(((Number) scoresMap.get("socialConfidence")).intValue());
                }

                aiStructInfo.setEmotionScores(emotionScoresObj);
            }

            // 提取emotion_history
            Object emotionHistory = aiAnalysisMap.get("emotion_history");
            if (emotionHistory instanceof List) {
                List<Map<String, Object>> historyList = (List<Map<String, Object>>) emotionHistory;
                List<EmotionScores> emotionHistoryList = new ArrayList<>();

                for (Map<String, Object> historyItem : historyList) {
                    EmotionScores emotionScoresObj = new EmotionScores();

                    // 设置情绪分数
                    if (historyItem.get("stability") instanceof Number) {//情绪稳定性
                        emotionScoresObj.setStability(((Number) historyItem.get("stability")).intValue());
                    }
                    if (historyItem.get("anxiety") instanceof Number) {//焦虑水平
                        emotionScoresObj.setAnxiety(((Number) historyItem.get("anxiety")).intValue());
                    }
                    if (historyItem.get("happiness") instanceof Number) {//幸福感
                        emotionScoresObj.setHappiness(((Number) historyItem.get("happiness")).intValue());
                    }
                    if (historyItem.get("socialConfidence") instanceof Number) {//社交自信
                        emotionScoresObj.setSocialConfidence(((Number) historyItem.get("socialConfidence")).intValue());
                    }

                    emotionHistoryList.add(emotionScoresObj);
                }

                aiStructInfo.setEmotionHistory(emotionHistoryList.toArray(new EmotionScores[0]));
            }

            // 提取recommendations
            Object recommendations = aiAnalysisMap.get("recommendations");
            if (recommendations instanceof List) {
                List<Map<String, Object>> recList = (List<Map<String, Object>>) recommendations;
                List<Recommendations> recommendationsList = new ArrayList<>();

                for (Map<String, Object> recItem : recList) {
                    Recommendations recommendationsObj = new Recommendations();

                    // 设置建议内容
                    if (recItem.get("title") instanceof String) {
                        recommendationsObj.setTitle((String) recItem.get("title"));
                    }
                    if (recItem.get("description") instanceof String) {
                        recommendationsObj.setPriority((String) recItem.get("priority"));
                    }
                    if (recItem.get("priority") instanceof String) {
                        recommendationsObj.setPriority((String) recItem.get("priority"));
                    }

                    recommendationsList.add(recommendationsObj);
                }

                aiStructInfo.setRecommendations(recommendationsList.toArray(new Recommendations[0]));
            }

            // 提取key_findings
            Object keyFindings = aiAnalysisMap.get("key_findings");
            if (keyFindings instanceof List) {
                aiStructInfo.setKeyFindings((String[]) keyFindings);
            }

            return aiStructInfo;

        } catch (Exception e) {
            log.error("解析AI分析结果到AiStructInfo失败", e);
            return null;
        }
    }

    /**
     * 更新儿童信息
     * @param updateChildInfoDTO 更新儿童信息参数
     */
    @Override
    public void updateChildInfo(UpdateChildInfoDTO updateChildInfoDTO) {
        Child child = new Child();
        BeanUtils.copyProperties(updateChildInfoDTO, child);

        boolean result = this.updateById(child);
        if (!result) {
            throw new BusinessException(CodeConstant.FAIL_CODE,"更新儿童信息失败");
        }
    }

    /**
     * 儿童注销登录
     */
    @Override
    public void logout() {
        // 清除ThreadLocal中的用户信息
        BaseContext.clear();
    }

    /*####################社工端####################*/
    /**
     * 获取儿童列表
     * @param childQueryDTO 儿童查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<ChildQueueVO> getChildList(ChildQueryDTO childQueryDTO) {
        // 设置分页参数
        int page = childQueryDTO.getPage() != null ? childQueryDTO.getPage() : 1;
        int pageSize = childQueryDTO.getPageSize() != null ? childQueryDTO.getPageSize() : 10;
        
        // 使用PageHelper进行分页查询
        PageHelper.startPage(page, pageSize);
        
        // 调用方法构建查询条件
        LambdaQueryWrapper<Child> queryWrapper = buildChildQueryWrapper(childQueryDTO);
        
        // 执行查询
        List<Child> children = this.list(queryWrapper);
        PageInfo<Child> pageInfo = new PageInfo<>(children);
        
        // 转换为ChildQueueVO列表
        List<ChildQueueVO> childQueueVOList = children.stream().map(child -> {
            ChildQueueVO childQueueVO = new ChildQueueVO();
            BeanUtils.copyProperties(child, childQueueVO);
            return childQueueVO;
        }).collect(java.util.stream.Collectors.toList());
        
        // 构建并返回分页结果
        return new PageResult<>(pageInfo.getTotal(), childQueueVOList);
    }

    /**
     * 构建儿童查询条件
     * @param childQueryDTO 查询条件DTO
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<Child> buildChildQueryWrapper(ChildQueryDTO childQueryDTO) {
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();

        // 儿童编号模糊查询
        if (childQueryDTO.getChildNo() != null) {
            queryWrapper.like(Child::getChildNo, childQueryDTO.getChildNo());
        }

        // 儿童姓名模糊查询
        if (childQueryDTO.getName() != null) {
            queryWrapper.like(Child::getName, childQueryDTO.getName());
        }

        // 最小年龄查询
        if (childQueryDTO.getMinAge() != null) {
            queryWrapper.ge(Child::getAge, childQueryDTO.getMinAge());
        }

        // 最大年龄查询
        if (childQueryDTO.getMaxAge() != null) {
            queryWrapper.le(Child::getAge, childQueryDTO.getMaxAge());
        }

        // 是否有新聊天记录查询
        if (childQueryDTO.getHasNewChat() != null) {
            queryWrapper.eq(Child::getHasNewChat, childQueryDTO.getHasNewChat());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Child::getCreateTime);

        return queryWrapper;
    }
}