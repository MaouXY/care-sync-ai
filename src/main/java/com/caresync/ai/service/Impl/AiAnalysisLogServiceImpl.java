package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.caresync.ai.model.DTO.AiAnalysisQueryDTO;
import com.caresync.ai.model.VO.AiAnalysisResultVO;
import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatMessage;
import com.caresync.ai.model.ai.ChatRequest;
import com.caresync.ai.model.entity.AiAnalysisLog;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AiAnalysisLogMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IAiChatRecordService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.IAiAnalysisLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.ArkUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class AiAnalysisLogServiceImpl extends ServiceImpl<AiAnalysisLogMapper, AiAnalysisLog> implements IAiAnalysisLogService {

    @Autowired
    private IChildService childService;

    @Autowired
    private IAiChatRecordService aiChatRecordService;

    @Autowired
    private AiAnalysisLogMapper aiAnalysisLogMapper;


    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ArkUtil arkUtil;

    /**
     * 获取AI分析记录列表
     * @param aiAnalysisQueryDTO 查询条件，包含儿童ID、姓名、开始时间、结束时间等查询参数（均允许空值）
     * @return 分页结果
     */
    @Override
    public PageResult<AiAnalysisResultVO> getAiAnalysisResults(AiAnalysisQueryDTO aiAnalysisQueryDTO) {
        // 设置分页参数
        int page = aiAnalysisQueryDTO.getPage() != null ? aiAnalysisQueryDTO.getPage() : 1;
        int pageSize = aiAnalysisQueryDTO.getPageSize() != null ? aiAnalysisQueryDTO.getPageSize() : 10;

        // 使用PageHelper进行分页查询
        PageHelper.startPage(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();

        // 按儿童姓名过滤（模糊匹配）
        if (aiAnalysisQueryDTO.getName() != null && !aiAnalysisQueryDTO.getName().isEmpty()) {
            queryWrapper.like(Child::getName, aiAnalysisQueryDTO.getName());
        }

        // 按潜在问题过滤（使用JSONB查询，利用GIN索引）
        if (aiAnalysisQueryDTO.getPotentialProblems() != null && !aiAnalysisQueryDTO.getPotentialProblems().isEmpty()) {
            queryWrapper.apply("ai_struct_info->>'potential_problems' ILIKE {0}", "%" + aiAnalysisQueryDTO.getPotentialProblems() + "%");
        }

        // 按情感趋势过滤（使用JSONB数组查询，利用GIN索引）
        if (aiAnalysisQueryDTO.getEmotionTrend() != null && !aiAnalysisQueryDTO.getEmotionTrend().isEmpty()) {
            // 在 {0} 后面添加 ::jsonb 来强制转换类型
            queryWrapper.apply("ai_struct_info->'emotion_trend' @> {0}::jsonb", "[\"" + aiAnalysisQueryDTO.getEmotionTrend() + "\"]");
        }

        // 按分析时间倒序排序
        queryWrapper.orderByDesc(Child::getAiAnalysisTime);

        // 执行查询
        List<Child> children = childService.list(queryWrapper);
        PageInfo<Child> pageInfo = new PageInfo<>(children);

        // 转换为VO列表
        List<AiAnalysisResultVO> records = children.stream()
                .map(child -> {
                    AiAnalysisResultVO aiAnalysisResultVO = new AiAnalysisResultVO();
                    aiAnalysisResultVO.setChildId(child.getId());
                    aiAnalysisResultVO.setChildName(child.getName());

                    // 设置分析时间为child表中的ai_analysis_time
                    aiAnalysisResultVO.setCreateTime(child.getAiAnalysisTime());

                    // 解析aiStructInfo中的结构化信息
                    if (child.getAiStructInfo() != null) {
                        parseStructuredInfo(child.getAiStructInfo(), aiAnalysisResultVO);
                    }

                    return aiAnalysisResultVO;
                })
                .collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), records);
    }

    /**
     * 解析analysisResult中的结构化信息
     * @param analysisResult 分析结果对象
     * @param aiAnalysisResultVO AI分析结果VO
     */
    private void parseStructuredInfo(Object analysisResult, AiAnalysisResultVO aiAnalysisResultVO) {
        try {
            // 将analysisResult转换为JsonNode
            JsonNode jsonNode;
            if (analysisResult instanceof String) {
                jsonNode = objectMapper.readTree((String) analysisResult);
            } else {
                // 如果不是String类型，尝试转换为JSON字符串再解析
                String jsonString = objectMapper.writeValueAsString(analysisResult);
                jsonNode = objectMapper.readTree(jsonString);
            }

            // 提取情感趋势标签
            if (jsonNode.has("emotion_trend")) {
                List<String> emotionTrendTags = new ArrayList<>();
                JsonNode emotionTrendNode = jsonNode.get("emotion_trend");
                if (emotionTrendNode.isArray()) {
                    for (JsonNode node : emotionTrendNode) {
                        emotionTrendTags.add(node.asText());
                    }
                }
                aiAnalysisResultVO.setEmotionTrendTags(emotionTrendTags);
            }

            // 提取潜在问题
            if (jsonNode.has("potential_problems")) {
                aiAnalysisResultVO.setPotentialProblems(jsonNode.get("potential_problems").asText());
            }

            // 提取分析摘要
            if (jsonNode.has("description")) {
                aiAnalysisResultVO.setDescription(jsonNode.get("description").asText());
            }

            // 提取最新分析时间
            if (jsonNode.has("latest_analysis")) {
                aiAnalysisResultVO.setLatestAnalysis(jsonNode.get("latest_analysis").asText());
            }

            // 提取情感评分指标
            if (jsonNode.has("emotion_scores")) {
                Map<String, Integer> emotionScores = new HashMap<>();
                JsonNode emotionScoresNode = jsonNode.get("emotion_scores");
                emotionScoresNode.fields().forEachRemaining(entry -> {
                    emotionScores.put(entry.getKey(), entry.getValue().asInt());
                });
                aiAnalysisResultVO.setEmotionScores(emotionScores);
            }

            // 提取情感历史记录
            if (jsonNode.has("emotion_history")) {
                List<AiAnalysisResultVO.EmotionHistoryItem> emotionHistory = new ArrayList<>();
                JsonNode emotionHistoryNode = jsonNode.get("emotion_history");
                if (emotionHistoryNode.isArray()) {
                    for (JsonNode node : emotionHistoryNode) {
                        AiAnalysisResultVO.EmotionHistoryItem item = new AiAnalysisResultVO.EmotionHistoryItem();
                        if (node.has("date")) {
                            item.setDate(node.get("date").asText());
                        }
                        if (node.has("scores")) {
                            Map<String, Integer> scores = new HashMap<>();
                            JsonNode scoresNode = node.get("scores");
                            scoresNode.fields().forEachRemaining(entry -> {
                                scores.put(entry.getKey(), entry.getValue().asInt());
                            });
                            item.setScores(scores);
                        }
                        emotionHistory.add(item);
                    }
                }
                aiAnalysisResultVO.setEmotionHistory(emotionHistory);
            }

            // 提取关键发现列表
            if (jsonNode.has("key_findings")) {
                List<String> keyFindings = new ArrayList<>();
                JsonNode keyFindingsNode = jsonNode.get("key_findings");
                if (keyFindingsNode.isArray()) {
                    for (JsonNode node : keyFindingsNode) {
                        keyFindings.add(node.asText());
                    }
                }
                aiAnalysisResultVO.setKeyFindings(keyFindings);
            }

            // 提取建议列表
            if (jsonNode.has("recommendations")) {
                List<AiAnalysisResultVO.Recommendation> recommendations = new ArrayList<>();
                JsonNode recommendationsNode = jsonNode.get("recommendations");
                if (recommendationsNode.isArray()) {
                    for (JsonNode node : recommendationsNode) {
                        AiAnalysisResultVO.Recommendation recommendation = new AiAnalysisResultVO.Recommendation();
                        if (node.has("title")) {
                            recommendation.setTitle(node.get("title").asText());
                        }
                        if (node.has("description")) {
                            recommendation.setDescription(node.get("description").asText());
                        }
                        if (node.has("priority")) {
                            recommendation.setPriority(node.get("priority").asText());
                        }
                        recommendations.add(recommendation);
                    }
                }
                aiAnalysisResultVO.setRecommendations(recommendations);
            }
        } catch (JsonProcessingException e) {
            // 解析失败时，设置默认值或记录日志
            log.info("解析AI分析结果失败: {}", e.getMessage());
            // 可以设置一些默认值
            aiAnalysisResultVO.setPotentialProblems("暂无潜在问题");
            aiAnalysisResultVO.setDescription("AI分析结果解析失败");
        }
    }

    /**
     * 根据儿童id生成AI分析结果 (现在要实现的!!!)
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    @Override
    public AiAnalysisResultVO generateAiAnalysis(Long id) {
        // 构建专业的情感分析提示词
        String systemPrompt = """
        你是一个专业的儿童情感分析专家，专门为留守儿童提供情感状态评估和干预建议。
        
        请根据提供的儿童聊天记录和基本信息，生成一份完整的情感分析报告。报告需要严格遵循以下JSON格式,只需输出JSON内容：
        
        {"emotion_trend":["情感标签1","情感标签2","情感标签3"],"core_needs":["核心需求1","核心需求2"],"potential_problems":"主要潜在问题描述","description":"综合分析摘要描述","emotion_scores":{"情绪稳定性":数值(0-100),"焦虑水平":数值(0-100),"幸福感":数值(0-100),"社交自信":数值(0-100)},"key_findings":["关键发现描述1","关键发现描述2","关键发现描述3"],"recommendations":[{"title":"建议标题","description":"建议详细描述"}]}
        
        分析要求：
        1. 情感趋势(emotion_trend)：基于聊天内容识别3-5个主要情感标签，如"孤独"、"平静"、"开心"、"焦虑"等
        2. 核心需求(core_needs)：识别儿童最迫切的情感需求，如"故事陪伴"、"学习辅导"、"社交互动"等
        3. 潜在问题(potential_problems)：识别可能存在的心理或社交问题，用简洁语言描述
        4. 情感评分(emotion_scores)：基于0-100分制评估四个维度，数值要合理反映儿童当前状态
        5. 关键发现(key_findings)：提供3-5个具体的观察发现，包含对比分析和具体数据
        6. 建议(recommendations)：提供2-5条具体可行的干预建议
        
        请确保分析结果客观、专业、具有可操作性，能够为社工制定服务方案提供有效依据。
        """;

        //获取该儿童信息的聊天记录
        //根据id查询儿童信息
        Child child = childService.getById(id);
        if (child == null) {
            log.info("未找到ID为{}的儿童信息", id);
            return null;
        }
        //获取该儿童的聊天记录,上次分析时间
        List<ChatMessage> chatMessages = aiChatRecordService.getChatMessagesByChildId(id, child.getAiAnalysisTime());
        if (chatMessages.isEmpty()) {
            log.info("儿童ID为{}的聊天记录为空,当前分析时间为{}", id, child.getAiAnalysisTime());
            return null;
        }
        //将之前的分结果也添加到提示词中
        String userPrompt = String.format(
                "儿童姓名：%s，儿童年龄：%d岁，儿童性别：%s，情感分析结果：%s",
                child.getName(), child.getAge(), child.getGender(),
                child.getAiStructInfo() != null ? child.getAiStructInfo() : "暂无情感分析结果"
        );

        //发起AI分析请求
        ChatContent chatContent = arkUtil.botChat(ChatRequest.builder().prompt(userPrompt).history(chatMessages).build(),systemPrompt);

        AiAnalysisLog aiAnalysisLog = new AiAnalysisLog();
        String completeAnalysisResult = "";
        // 保存分析结果到数据库,并更新儿童的分析时间和AI分析信息
        if (chatContent != null) {
            // 构建AiAnalysisLog实体
            aiAnalysisLog.setChildId(id);
            
            // 构建完整的分析结果，包含latest_analysis和emotion_history
            completeAnalysisResult = buildCompleteAnalysisResult(chatContent.getContent(), id, child);

            //log.info("完整的分析结果: {}", completeAnalysisResult);

            // 直接使用JSON字符串，JsonbTypeHandler会处理类型转换
            aiAnalysisLog.setAnalysisResult(completeAnalysisResult);
            
            aiAnalysisLog.setTriggerType("AUTO"); // 设置触发类型为自动
            aiAnalysisLog.setCreateTime(LocalDateTime.now());
            
            // 保存到数据库
            aiAnalysisLogMapper.insert(aiAnalysisLog);
            
            // 更新儿童的分析时间和AI分析信息
            child.setAiAnalysisTime(LocalDateTime.now());
            child.setAiStructInfo(completeAnalysisResult); // 将AI分析结果保存到儿童的结构化信息中
            childService.updateById(child);
            
            log.info("AI分析结果已保存，分析结果: {}", completeAnalysisResult);
        }
        
        // 创建并返回AI分析结果VO
        AiAnalysisResultVO result = new AiAnalysisResultVO();
        //返回id
        result.setId(aiAnalysisLog.getId());
        result.setChildId(child.getId());
        result.setChildName(child.getName() + "(" + child.getAge() + "岁)");
        result.setCreateTime(LocalDateTime.now());
        // 设置analysisResult为null，解析后的数据直接放在VO对象中
        result.setAnalysisResult(completeAnalysisResult);

//        // 设置完整的分析结果到aiStructInfo
//        result.setAiStructInfo(chatContent != null ? completeAnalysisResult : null);
//        // 解析结构化信息到VO对象的各个字段
//        if (chatContent != null) {
//            parseStructuredInfo(completeAnalysisResult, result);
//        }

        return result;
    }

    /**
     * 构建完整的分析结果，包含latest_analysis和emotion_history ????
     * @param aiAnalysisResult AI分析结果JSON字符串
     * @param childId 儿童ID
     * @param child 儿童实体
     * @return 完整的分析结果JSON字符串
     */
    private String buildCompleteAnalysisResult(String aiAnalysisResult, Long childId, Child child) {
        try {
            // 解析AI分析结果
            JsonNode analysisNode = objectMapper.readTree(aiAnalysisResult);
            
            // 创建完整的结果对象
            Map<String, Object> completeResult = new HashMap<>();
            
            // 复制原有的分析结果字段
            if (analysisNode.has("emotion_trend")) {
                completeResult.put("emotion_trend", objectMapper.convertValue(analysisNode.get("emotion_trend"), List.class));
            }
            if (analysisNode.has("core_needs")) {
                completeResult.put("core_needs", objectMapper.convertValue(analysisNode.get("core_needs"), List.class));
            }
            if (analysisNode.has("potential_problems")) {
                completeResult.put("potential_problems", analysisNode.get("potential_problems").asText());
            }
            if (analysisNode.has("description")) {
                completeResult.put("description", analysisNode.get("description").asText());
            }
            if (analysisNode.has("emotion_scores")) {
                completeResult.put("emotion_scores", objectMapper.convertValue(analysisNode.get("emotion_scores"), Map.class));
            }
            if (analysisNode.has("key_findings")) {
                completeResult.put("key_findings", objectMapper.convertValue(analysisNode.get("key_findings"), List.class));
            }
            if (analysisNode.has("recommendations")) {
                completeResult.put("recommendations", objectMapper.convertValue(analysisNode.get("recommendations"), List.class));
            }
            
            // 添加latest_analysis字段（当前时间）
            completeResult.put("latest_analysis", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 构建emotion_history
            List<Map<String, Object>> emotionHistory = new ArrayList<>();
            
            // 获取之前的分析记录
            QueryWrapper<AiAnalysisLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("child_id", childId)
                       .orderByAsc("create_time");
            List<AiAnalysisLog> previousLogs = aiAnalysisLogMapper.selectList(queryWrapper);
            
            // 添加之前的情感历史记录
            for (AiAnalysisLog aiLog : previousLogs) {
                try {
                    JsonNode logNode = objectMapper.readTree(aiLog.getAnalysisResult().toString());
                    if (logNode.has("emotion_scores") && logNode.has("latest_analysis")) {
                        Map<String, Object> historyItem = new HashMap<>();
                        historyItem.put("date", logNode.get("latest_analysis").asText());
                        historyItem.put("scores", objectMapper.convertValue(logNode.get("emotion_scores"), Map.class));
                        emotionHistory.add(historyItem);
                    }
                } catch (Exception e) {
                    log.error("解析历史分析记录失败: {}", e.getMessage());
                }
            }
            
            // 添加当前的情感评分到历史记录
            if (analysisNode.has("emotion_scores")) {
                Map<String, Object> currentItem = new HashMap<>();
                currentItem.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                currentItem.put("scores", objectMapper.convertValue(analysisNode.get("emotion_scores"), Map.class));
                emotionHistory.add(currentItem);
            }
            
            // 添加emotion_history到完整结果
            completeResult.put("emotion_history", emotionHistory);

            String s = objectMapper.writeValueAsString(completeResult);
            log.info("解析构建结果{}",s);
            
            return s;
            
        } catch (Exception e) {
            log.error("构建完整分析结果失败: {}", e.getMessage());
            // 如果构建失败，返回原始分析结果
            return aiAnalysisResult;
        }
    }

    /**
     * 获取AI分析结果详情
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    @Override
    public AiAnalysisResultVO getAiAnalysisDetail(Long id) {
        // 根据ID查询儿童信息
        Child child = childService.getById(id);
        if (child == null) {
            log.info("未找到ID为{}的儿童信息", id);
            return null;
        }

        // 创建AI分析结果VO对象
        AiAnalysisResultVO aiAnalysisResultVO = new AiAnalysisResultVO();

        // 设置基本信息
        aiAnalysisResultVO.setChildId(child.getId());
        aiAnalysisResultVO.setChildName(child.getName() + "(" + child.getAge() + "岁)");
        aiAnalysisResultVO.setCreateTime(child.getAiAnalysisTime());

        // 直接设置aiStructInfo字段
        aiAnalysisResultVO.setAiStructInfo(child.getAiStructInfo());

        // 解析aiStructInfo中的结构化信息
        if (child.getAiStructInfo() != null) {
            parseStructuredInfo(child.getAiStructInfo(), aiAnalysisResultVO);
        }

        return aiAnalysisResultVO;
    }

}