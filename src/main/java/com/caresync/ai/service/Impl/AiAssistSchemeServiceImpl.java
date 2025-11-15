package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.GenerateSchemeDTO;
import com.caresync.ai.model.DTO.SchemeQueryDTO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatRequest;
import com.caresync.ai.model.entity.AiAssistScheme;
import com.caresync.ai.model.entity.AiAnalysisLog;
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AiAssistSchemeMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IAiAnalysisLogService;
import com.caresync.ai.service.IAiAssistSchemeService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.ISocialWorkerService;
import com.caresync.ai.service.IAssistTrackLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.ArkUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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

    @Autowired
    private ArkUtil arkUtil;

    @Autowired
    private IAssistTrackLogService assistTrackLogService;

    /**
     * 获取辅助方案列表
     *
     * @param schemeQueryDTO 查询条件，包含儿童ID、社工ID、方案状态、开始时间、结束时间等查询参数（均允许空值）
     * @return 分页结果
     */
    @Override
    public PageResult<AssistSchemeVO> getSchemeList(SchemeQueryDTO schemeQueryDTO) {
        // 设置分页参数
        int page = schemeQueryDTO.getPage() != null ? schemeQueryDTO.getPage() : 1;
        int pageSize = schemeQueryDTO.getPageSize() != null ? schemeQueryDTO.getPageSize() : 10;

        // 使用PageHelper进行分页查询
        PageHelper.startPage(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<AiAssistScheme> queryWrapper = new LambdaQueryWrapper<>();

        // 按儿童ID过滤
        if (schemeQueryDTO.getChildId() != null) {
            queryWrapper.eq(AiAssistScheme::getChildId, schemeQueryDTO.getChildId());
        }

        // 按社工ID过滤
        if (schemeQueryDTO.getWorkerId() != null) {
            queryWrapper.eq(AiAssistScheme::getWorkerId, schemeQueryDTO.getWorkerId());
        }

        // 按方案状态过滤
        if (schemeQueryDTO.getSchemeStatus() != null) {
            queryWrapper.eq(AiAssistScheme::getSchemeStatus, schemeQueryDTO.getSchemeStatus());
        }

        // 按方案名称过滤（通过关联child表查询儿童姓名）
        if (schemeQueryDTO.getName() != null && !schemeQueryDTO.getName().isEmpty()) {
            queryWrapper.inSql(AiAssistScheme::getChildId,
                    "SELECT id FROM child WHERE name ILIKE '%" + schemeQueryDTO.getName() + "%'");
        }

        // 按时间范围过滤
        if (schemeQueryDTO.getStartDate() != null) {
            queryWrapper.ge(AiAssistScheme::getCreateTime, schemeQueryDTO.getStartDate());
        }
        if (schemeQueryDTO.getEndDate() != null) {
            queryWrapper.le(AiAssistScheme::getCreateTime, schemeQueryDTO.getEndDate());
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(AiAssistScheme::getCreateTime);

        // 执行查询
        List<AiAssistScheme> schemes = this.list(queryWrapper);
        PageInfo<AiAssistScheme> pageInfo = new PageInfo<>(schemes);

        // 转换为VO并补充信息
        List<AssistSchemeVO> schemeVOList = convertToSchemeVOList(schemes);

        // 构建返回结果
        return new PageResult<>(pageInfo.getTotal(), schemeVOList);
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
     *
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
                    Map<String, Object> aiStructInfoMap = objectMapper.readValue(aiStructInfoStr, new TypeReference<Map<String, Object>>() {
                    });
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
                Map<String, Object> aiSuggestionsMap = objectMapper.readValue(aiSuggestions, new TypeReference<Map<String, Object>>() {
                });

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
     *
     * @param generateSchemeDTO 生成方案DTO，包含儿童ID、社工ID、目标、周期、额外信息等
     * @return 辅助方案VO
     */
    @Override
    public AssistSchemeVO generateScheme(GenerateSchemeDTO generateSchemeDTO) {
        log.info("开始生成AI服务方案，儿童ID: {}", generateSchemeDTO.getChildId());

        String systemPrompt = """
                你是一个专业的社会工作者，专门为留守儿童提供情感陪伴服务和方案设计。
                        
                请根据儿童的AI分析结果，生成一个详细的服务方案。方案需要严格遵循以下JSON格式，只需输出JSON内容：
                        
                {
                  "target_suggest": ["目标1", "目标2", "目标3"],
                  "measures_suggest": [
                    {
                      "week": "第一周阶段标题",
                      "details": [
                        {"content": "具体任务内容1", "status": "pending"},
                        {"content": "具体任务内容2", "status": "pending"},
                        {"content": "具体任务内容3", "status": "pending"}
                      ]
                    },
                    {
                      "week": "第二周阶段标题",
                      "details": [
                        {"content": "具体任务内容1", "status": "pending"},
                        {"content": "具体任务内容2", "status": "pending"},
                        {"content": "具体任务内容3", "status": "pending"}
                      ]
                    }
                  ]
                }
                        
                方案生成要求：
                1. 目标建议(target_suggest)：基于儿童的情感分析结果，提出3-5个具体、可衡量的服务目标
                2. 服务措施(measures_suggest)：设计4-6周的服务计划，每周包含3-5个具体可执行的任务
                3. 每周标题(week)：基于儿童的具体情感需求，为每周设计专业、有意义的阶段标题
                4. 任务内容(content)：每个任务要具体、可操作，符合儿童年龄特点，包含具体活动和方法
                5. 状态(status)：所有任务状态统一设置为"pending"，不需要生成assist_track_log_id
                        
                每周标题设计要求：
                - 标题要体现专业性和针对性，反映该周的核心服务重点
                - 标题要简洁明了，便于社工理解和执行
                - 标题要体现服务进程的递进性，如：信任建立→情绪识别→社交技能→巩固提升
                - 标题要基于儿童的具体情感分析结果进行个性化设计
                        
                内容要求：
                - 目标要聚焦儿童的核心情感需求，如安全感建立、情绪管理、社交能力提升等
                - 措施要循序渐进，从建立信任关系到技能培养再到巩固提升
                - 任务要具体可行，包含具体的活动、游戏、沟通方式等
                - 要体现专业性和针对性，基于儿童的具体情感分析结果
                        
                请确保方案内容专业、具体、可操作，能够为社工提供清晰的执行指导。
                """;
        //以下是方案结构
      /*  -- 子任务完成状态直接通过ai_suggestions JSON字段中的details数组实现
                -- details数组中的每个子任务对象包含content(任务内容)和status(完成状态)字段
                -- status字段支持三种状态：pending(待处理)/in_progress(进行中)/completed(已完成)
                -- ai_suggestions结构示例（适配服务计划页面）
        -- {
                --   "target_suggest": [ -- 方案目标
                --     "降低孤独焦虑，建立积极心态",
                --     "增强情绪管理，正确表达感受",
                --     "提升社交能力，改善人际沟通"
                        --   ], -- 方案目标
                --   "measures_suggest": [ -- 服务措施
                --     {
            --       "week": "建立信任关系",
                    --       "details": [
            --         {"content": "初次见面，了解小明的兴趣爱好和日常生活情况。", "status": "completed","assist_track_log_id":1}, --status和assist_track_log_id为使用代码添加,不需要ai生成
            --         {"content": "一起参与小明感兴趣的活动（如绘画、下棋），建立初步信任。", "status": "completed","assist_track_log_id":2},
            --         {"content": "与小明约定每周固定的见面时间，增加安全感。", "status": "in_progress","assist_track_log_id":3}
            --       ]
            --     },
        --     {
                --       "week": "情绪识别与表达",
                --       "details": [
        --         {"content": "通过情绪卡片游戏，帮助小明识别不同的情绪。", "status": "pending","assist_track_log_id":4},
        --         {"content": "引导小明用绘画的方式表达自己的内心感受。", "status": "pending","assist_track_log_id":5},
        --         {"content": "教授简单的情绪调节方法，如深呼吸、倾诉等。", "status": "pending","assist_track_log_id":6}
        --       ]
        --     },
        --     {
                --       "week": "社交技能培养",
                --       "details": [
        --         {"content": "组织小组活动，鼓励小明与其他小朋友互动。", "status": "pending","assist_track_log_id":7},
        --         {"content": "角色扮演练习，学习如何与他人友好沟通。", "status": "pending","assist_track_log_id":8},
        --         {"content": "分享正面社交经验，增强小明的自信心。", "status": "pending","assist_track_log_id":9}
        --       ]
        --     },
        --     {
                --       "week": "总结与展望",
                --       "details": [
        --         {"content": "回顾四周的变化，肯定小明的进步。", "status": "pending","assist_track_log_id":10},
        --         {"content": "共同制定后续计划，帮助小明保持积极状态。", "status": "pending","assist_track_log_id":11},
        --         {"content": "与家长沟通，分享小明的成长和需要继续关注的方面。", "status": "pending","assist_track_log_id":12}
        --       ]
        --     }
        --   ]
        -- }*/

        // 1. 获取儿童信息
        Child child = childService.getById(generateSchemeDTO.getChildId());
        if (child == null) {
            log.error("未找到ID为{}的儿童信息", generateSchemeDTO.getChildId());
            throw new RuntimeException("儿童信息不存在");
        }

        // 3. 解析AI分析结果
        String analysisResult = parseAnalysisResult(child.getAiStructInfo());

        ChatRequest request = ChatRequest.builder()
                .prompt("儿童基本信息：姓名：" + child.getName() + "，年龄：" + child.getAge() + "岁，性别：" + child.getGender() +
                        "\nAI情感分析结果：" + analysisResult +
                        "\n服务目标要求：" + (generateSchemeDTO.getAdditionalInfo() != null ? generateSchemeDTO.getAdditionalInfo() : "基于儿童情感需求制定个性化服务方案") +
                        "\n请基于以上信息，生成符合JSON格式要求的服务方案。" +
                        "\n特别注意：请为每周设计专业、有意义的阶段标题，标题要体现该周的核心服务重点，如'信任关系建立'、'情绪识别训练'、'社交技能培养'等。")
                .build();

        // 4. 生成AI建议
        ChatContent aiSuggestions = arkUtil.botChat(request, systemPrompt);

        // 5. 创建服务方案
        AiAssistScheme scheme = new AiAssistScheme();
        scheme.setChildId(child.getId());
        scheme.setWorkerId(BaseContext.getCurrentId()); // 默认社工ID，实际应该从登录信息获取
        scheme.setTarget(generateSchemeDTO.getAdditionalInfo() != null ?
                generateSchemeDTO.getAdditionalInfo() : "缓解孤独感，提升社交能力");
        //scheme.setMeasures(generateSchemeDTO.getMeasures() != null ? generateSchemeDTO.getMeasures() : new String[]{"建立信任关系", "情绪识别与表达", "社交技能培养", "总结与展望"});

        // 解析AI建议中的周数来确定周期
        int cycle = parseCycleFromAiSuggestions(aiSuggestions.getContent());
        scheme.setCycle(cycle);

        scheme.setSchemeStatus("DRAFT");

        // 将AI建议字符串转换为JSON对象
        try {
            Object aiSuggestionsJson = objectMapper.readValue(aiSuggestions.getContent(), Object.class);
            scheme.setAiSuggestions(aiSuggestionsJson);
        } catch (Exception e) {
            log.error("AI建议JSON解析失败，使用原始字符串", e);
            scheme.setAiSuggestions(aiSuggestions.getContent());
        }

        // 6. 保存服务方案
        boolean saveSuccess = this.save(scheme);
        if (!saveSuccess) {
            log.error("保存服务方案失败，儿童ID: {}", child.getId());
            throw new RuntimeException("生成服务方案失败");
        }

        log.info("成功生成服务方案，方案ID: {}", scheme.getId());

        // 7. 保存子任务记录到assist_track_log表
        try {
            saveTaskDetailsToTrackLog(scheme, child, aiSuggestions.getContent());
            log.info("成功保存子任务记录到assist_track_log表");
        } catch (Exception e) {
            log.error("保存子任务记录失败，但服务方案已保存成功", e);
            // 子任务保存失败不影响主流程，继续执行
        }

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
        //vo.setAiAnalysisId(scheme.getAiAnalysisId());
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
     * 解析AI建议中的周数来确定周期
     *
     * @param aiSuggestions AI建议的JSON字符串
     * @return 解析出的周数，如果解析失败则返回默认值7
     */
    private int parseCycleFromAiSuggestions(String aiSuggestions) {
        try {
            if (aiSuggestions == null || aiSuggestions.trim().isEmpty()) {
                log.warn("AI建议为空，使用默认周期7周");
                return 7;
            }

            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(aiSuggestions);

            // 获取measures_suggest数组
            JsonNode measuresSuggestNode = rootNode.path("measures_suggest");
            if (measuresSuggestNode.isMissingNode() || !measuresSuggestNode.isArray()) {
                log.warn("measures_suggest字段不存在或不是数组，使用默认周期7周");
                return 7;
            }

            // 计算周数（measures_suggest数组的长度）
            int cycle = measuresSuggestNode.size();

            // 验证周数合理性
            if (cycle <= 0) {
                log.warn("解析出的周数{}不合理，使用默认周期7周", cycle);
                return 7;
            }

            // 限制最大周数为12周，避免异常数据
            if (cycle > 12) {
                log.warn("解析出的周数{}超过最大值，限制为12周", cycle);
                return 12;
            }

            log.info("成功解析AI建议，确定服务周期为{}周", cycle);
            return cycle;

        } catch (Exception e) {
            log.error("解析AI建议周数失败，使用默认周期7周", e);
            return 7;
        }
    }

    /**
     * 保存子任务记录到assist_track_log表
     *
     * @param scheme               服务方案
     * @param child                儿童信息
     * @param aiSuggestionsContent AI建议内容
     */
    private void saveTaskDetailsToTrackLog(AiAssistScheme scheme, Child child, String aiSuggestionsContent) {
        if (aiSuggestionsContent == null || aiSuggestionsContent.trim().isEmpty()) {
            log.warn("AI建议内容为空，跳过保存子任务记录");
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(aiSuggestionsContent);
            JsonNode measuresSuggestNode = rootNode.path("measures_suggest");

            if (!measuresSuggestNode.isArray()) {
                log.warn("measures_suggest不是数组格式，跳过保存子任务记录");
                return;
            }

            // 获取当前登录的社工ID
            Long workerId = getCurrentWorkerId();
            if (workerId == null) {
                log.warn("无法获取当前社工ID，跳过保存子任务记录");
                return;
            }

            // 遍历每周的服务措施
            for (int weekIndex = 0; weekIndex < measuresSuggestNode.size(); weekIndex++) {
                JsonNode weekNode = measuresSuggestNode.get(weekIndex);
                JsonNode detailsNode = weekNode.path("details");

                if (detailsNode.isArray()) {
                    // 遍历每周的子任务
                    for (int taskIndex = 0; taskIndex < detailsNode.size(); taskIndex++) {
                        JsonNode taskNode = detailsNode.get(taskIndex);

                        // 创建子任务记录
                        AssistTrackLog trackLog = new AssistTrackLog();
                        trackLog.setSchemeId(scheme.getId());
                        trackLog.setChildId(child.getId());
                        trackLog.setWorkerId(workerId);
                        trackLog.setWeek(weekIndex + 1); // 周次从1开始
                        trackLog.setCompletionStatus("pending");

                        // 设置记录内容
                        if (taskNode.has("content")) {
                            trackLog.setRecordContent(taskNode.get("content").asText());
                        } else {
                            trackLog.setRecordContent("未定义的任务内容");
                        }

                        // 保存子任务记录
                        boolean saveSuccess = assistTrackLogService.save(trackLog);
                        if (saveSuccess) {
                            log.info("成功保存第{}周第{}个子任务，记录ID: {}",
                                    weekIndex + 1, taskIndex + 1, trackLog.getId());
                        } else {
                            log.error("保存第{}周第{}个子任务失败", weekIndex + 1, taskIndex + 1);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析AI建议并保存子任务记录失败", e);
            throw new RuntimeException("保存子任务记录失败", e);
        }
    }

    /**
     * 获取当前登录的社工ID
     *
     * @return 社工ID
     */
    private Long getCurrentWorkerId() {
        try {
            // 这里需要根据实际的认证机制获取当前用户ID
            // 暂时返回一个默认值，实际项目中需要根据认证系统实现
            return 1L; // 默认社工ID
        } catch (Exception e) {
            log.error("获取当前社工ID失败", e);
            return null;
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

        // 批量获取儿童年龄信息
        Map<Long, String> childAgeMap = childIds.stream()
                .collect(Collectors.toMap(id -> id, id -> {
                    ChildInfoVO childInfo = childService.getChildInfo(id);
                    return childInfo != null && childInfo.getAge() != null ? 
                           childInfo.getAge().toString() + "岁" : "未知年龄";
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
            //vo.setAiSuggestions(scheme.getAiSuggestions());
            vo.setWorkerAdjustReason(scheme.getWorkerAdjustReason());
            //vo.setAiAnalysisId(scheme.getAiAnalysisId());
            // 注意：evaluationIndex字段由parseAiSuggestions方法设置，这里不重复设置
            vo.setCreateTime(scheme.getCreateTime());
            vo.setUpdateTime(scheme.getUpdateTime());
            // 补充扩展字段
            vo.setChildName(childNameMap.getOrDefault(scheme.getChildId(), "未知儿童"));
            vo.setChildAge(childAgeMap.getOrDefault(scheme.getChildId(), "未知年龄"));
            vo.setWorkerName(workerNameMap.getOrDefault(scheme.getWorkerId(), "未知社工"));

            // 解析ai_suggestions中的结构化信息
            parseAiSuggestions(scheme.getAiSuggestions(), vo);

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 解析ai_suggestions中的结构化信息
     *
     * @param aiSuggestions  AI建议JSON对象
     * @param assistSchemeVO 服务方案VO
     */
    private void parseAiSuggestions(Object aiSuggestions, AssistSchemeVO assistSchemeVO) {
        try {
            if (aiSuggestions == null) {
                return;
            }

            // 将aiSuggestions转换为JsonNode
            JsonNode jsonNode;
            if (aiSuggestions instanceof String) {
                jsonNode = objectMapper.readTree((String) aiSuggestions);
            } else {
                // 如果不是String类型，尝试转换为JSON字符串再解析
                String jsonString = objectMapper.writeValueAsString(aiSuggestions);
                jsonNode = objectMapper.readTree(jsonString);
            }

            // 提取方案目标
            if (jsonNode.has("target_suggest")) {
                List<String> targetSuggest = new ArrayList<>();
                JsonNode targetSuggestNode = jsonNode.get("target_suggest");
                if (targetSuggestNode.isArray()) {
                    for (JsonNode node : targetSuggestNode) {
                        targetSuggest.add(node.asText());
                    }
                }
                // 设置到VO的targetSuggest字段
                assistSchemeVO.setTargetSuggest(targetSuggest);
            }

            // 提取服务措施
            if (jsonNode.has("measures_suggest")) {
                List<Map<String, Object>> measuresSuggest = new ArrayList<>();
                JsonNode measuresSuggestNode = jsonNode.get("measures_suggest");
                if (measuresSuggestNode.isArray()) {
                    for (JsonNode measureNode : measuresSuggestNode) {
                        Map<String, Object> measure = new HashMap<>();

                        // 提取周次标题
                        if (measureNode.has("week")) {
                            measure.put("week", measureNode.get("week").asText());
                        }

                        // 提取任务详情
                        if (measureNode.has("details")) {
                            List<Map<String, Object>> details = new ArrayList<>();
                            JsonNode detailsNode = measureNode.get("details");
                            if (detailsNode.isArray()) {
                                for (JsonNode detailNode : detailsNode) {
                                    Map<String, Object> detail = new HashMap<>();

                                    // 提取任务内容
                                    if (detailNode.has("content")) {
                                        detail.put("content", detailNode.get("content").asText());
                                    }

                                    // 提取任务状态
                                    if (detailNode.has("status")) {
                                        detail.put("status", detailNode.get("status").asText());
                                    }

                                    // 提取跟踪日志ID
                                    if (detailNode.has("assist_track_log_id")) {
                                        detail.put("assist_track_log_id", detailNode.get("assist_track_log_id").asLong());
                                    }

                                    details.add(detail);
                                }
                            }
                            measure.put("details", details);
                        }

                        measuresSuggest.add(measure);
                    }
                }
                // 设置到VO的measuresSuggest字段
                assistSchemeVO.setMeasuresSuggest(measuresSuggest);
            }

            // 提取评估指标（如果存在）
            if (jsonNode.has("evaluation_index")) {
                Map<String, Object> evaluationIndex = new HashMap<>();
                JsonNode evaluationIndexNode = jsonNode.get("evaluation_index");
                evaluationIndexNode.fields().forEachRemaining(entry -> {
                    evaluationIndex.put(entry.getKey(), entry.getValue());
                });
                // 设置到VO的evaluationIndex字段
                assistSchemeVO.setEvaluationIndex(evaluationIndex);
            }

        } catch (Exception e) {
            // 解析失败时记录日志
            log.error("解析AI建议失败: {}", e.getMessage());
        }
    }

    /**
     * 解析evaluation_index中的评估指标信息
     *
     * @param evaluationIndex 评估指标JSON对象
     * @param assistSchemeVO  服务方案VO
     */
    private void parseEvaluationIndex(Object evaluationIndex, AssistSchemeVO assistSchemeVO) {
        try {
            if (evaluationIndex == null) {
                return;
            }

            // 将evaluationIndex转换为JsonNode
            JsonNode jsonNode;
            if (evaluationIndex instanceof String) {
                jsonNode = objectMapper.readTree((String) evaluationIndex);
            } else {
                // 如果不是String类型，尝试转换为JSON字符串再解析
                String jsonString = objectMapper.writeValueAsString(evaluationIndex);
                jsonNode = objectMapper.readTree(jsonString);
            }

            // 提取各项评估指标
            Map<String, Object> evaluationMap = new HashMap<>();
            jsonNode.fields().forEachRemaining(entry -> {
                evaluationMap.put(entry.getKey(), entry.getValue());
            });

            // 设置到VO的evaluationIndex字段
            assistSchemeVO.setEvaluationIndex(evaluationMap);

        } catch (Exception e) {
            // 解析失败时记录日志
            log.error("解析评估指标失败: {}", e.getMessage());
        }
    }
}