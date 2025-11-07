package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Override
    public PageResult<AiAnalysisResultVO> getAiAnalysisResults(AiAnalysisQueryDTO aiAnalysisQueryDTO) {
        // 获取所有儿童信息
        List<Child> allChildren = childService.list();

        // 创建分页查询对象，用于计算分页
        int page = aiAnalysisQueryDTO.getPage() != null ? aiAnalysisQueryDTO.getPage() : 1;
        int pageSize = aiAnalysisQueryDTO.getPageSize() != null ? aiAnalysisQueryDTO.getPageSize() : 10;
        int startIndex = (page - 1) * pageSize;

        // 转换实体列表为VO列表并应用过滤条件
        List<AiAnalysisResultVO> records = allChildren.stream()
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
                // 应用过滤条件
                .filter(vo -> {
                    // 按儿童姓名过滤（模糊匹配）
                    if (aiAnalysisQueryDTO.getName() != null && !aiAnalysisQueryDTO.getName().isEmpty()) {
                        if (vo.getChildName() == null || !vo.getChildName().contains(aiAnalysisQueryDTO.getName())) {
                            return false;
                        }
                    }

                    // 按潜在问题过滤（模糊匹配）
                    if (aiAnalysisQueryDTO.getPotentialProblems() != null && !aiAnalysisQueryDTO.getPotentialProblems().isEmpty()) {
                        if (vo.getPotentialProblems() == null || !vo.getPotentialProblems().contains(aiAnalysisQueryDTO.getPotentialProblems())) {
                            return false;
                        }
                    }

                    // 按情感趋势过滤（模糊匹配）
                    if (aiAnalysisQueryDTO.getEmotionTrend() != null && !aiAnalysisQueryDTO.getEmotionTrend().isEmpty()) {
                        if (vo.getEmotionTrendTags() == null || !vo.getEmotionTrendTags().stream().anyMatch(tag -> tag.contains(aiAnalysisQueryDTO.getEmotionTrend()))) {
                            return false;
                        }
                    }

                    return true;
                })
                // 按分析时间倒序排序
                .sorted((v1, v2) -> {
                    if (v1.getCreateTime() == null) return 1;
                    if (v2.getCreateTime() == null) return -1;
                    return v2.getCreateTime().compareTo(v1.getCreateTime());
                })
                // 分页处理
                .skip(startIndex)
                .limit(pageSize)
                .collect(Collectors.toList());

        // 获取符合条件的总记录数（再次执行过滤但不分页）
        long total = allChildren.stream()
                .map(child -> {
                    AiAnalysisResultVO aiAnalysisResultVO = new AiAnalysisResultVO();
                    aiAnalysisResultVO.setChildId(child.getId());
                    aiAnalysisResultVO.setChildName(child.getName());
                    aiAnalysisResultVO.setCreateTime(child.getAiAnalysisTime());

                    if (child.getAiStructInfo() != null) {
                        parseStructuredInfo(child.getAiStructInfo(), aiAnalysisResultVO);
                    }

                    return aiAnalysisResultVO;
                })
                .filter(vo -> {
                    // 应用相同的过滤条件
                    if (aiAnalysisQueryDTO.getName() != null && !aiAnalysisQueryDTO.getName().isEmpty()) {
                        if (vo.getChildName() == null || !vo.getChildName().contains(aiAnalysisQueryDTO.getName())) {
                            return false;
                        }
                    }

                    if (aiAnalysisQueryDTO.getPotentialProblems() != null && !aiAnalysisQueryDTO.getPotentialProblems().isEmpty()) {
                        if (vo.getPotentialProblems() == null || !vo.getPotentialProblems().contains(aiAnalysisQueryDTO.getPotentialProblems())) {
                            return false;
                        }
                    }

                    if (aiAnalysisQueryDTO.getEmotionTrend() != null && !aiAnalysisQueryDTO.getEmotionTrend().isEmpty()) {
                        if (vo.getEmotionTrendTags() == null || !vo.getEmotionTrendTags().stream().anyMatch(tag -> tag.contains(aiAnalysisQueryDTO.getEmotionTrend()))) {
                            return false;
                        }
                    }

                    return true;
                })
                .count();

        // 构建分页结果
        return new PageResult<>(total, records);
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
            log("解析AI分析结果失败: {}", e.getMessage());
            // 可以设置一些默认值
            aiAnalysisResultVO.setPotentialProblems("暂无潜在问题");
            aiAnalysisResultVO.setDescription("AI分析结果解析失败");
        }
    }

    /**
     * 根据儿童id生成AI分析结果
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    @Override
    public AiAnalysisResultVO generateAiAnalysis(Long id) {
        //构建提示词
        String systemPrompt = "你是一个专业的儿童情感分析助手，能够根据儿童的聊天记录和情感分析结果，生成详细的情感分析报告。";
        // TODO 权美珊 完善提示词 下面是需要生成的实例 "--"后面的是注释
        /**
         * -- ai_struct_info扩展结构示例（适配AI分析结果展示页）
         * -- {
         * --   "emotion_trend": [
         * --     "孤独",
         * --     "平静",
         * --     "开心"
         * --   ], -- 情感趋势标签（对应图表X轴）
         * --   "core_needs": [
         * --     "故事陪伴",
         * --     "学习辅导"
         * --   ], -- 核心需求标签（展示在摘要卡片）
         * --   "potential_problems": "沟通较少", -- 潜在问题标签（红色预警展示）
         * --   "description": "该儿童存在孤独感，沟通较少，核心需求为故事陪伴, 学习辅导", -- 分析摘要（综合描述）
         * --   "latest_analysis": "2023-07-15 15:30:00", -- 最新分析时间（页面右上角标注）
         * --   "emotion_scores": {
         * --     "情绪稳定性": 75,
         * --     "焦虑水平": 35,
         * --     "幸福感": 65,
         * --     "社交自信": 45
         * --   }, -- 情感评分指标（雷达图数据）
         * --   "emotion_history": [
         * --     {
         * --       "date": "2023-07-15",
         * --       "scores": {
         * --         "情绪稳定性": 75,
         * --         "焦虑水平": 35,
         * --         "幸福感": 65,
         * --         "社交自信": 45
         * --       }
         * --     },
         * --     {
         * --       "date": "2023-07-08",
         * --       "scores": {
         * --         "情绪稳定性": 60,
         * --         "焦虑水平": 45,
         * --         "幸福感": 60,
         * --         "社交自信": 45
         * --       }
         * --     },
         * --     {
         * --       "date": "2023-07-01",
         * --       "scores": {
         * --         "情绪稳定性": 55,
         * --         "焦虑水平": 50,
         * --         "幸福感": 55,
         * --         "社交自信": 40
         * --       }
         * --     }
         * --   ], -- 情感历史记录（趋势图数据）
         * --   "key_findings": [
         * --     "情绪稳定性较上周提升了15%，表现出更好的情绪调节能力。",
         * --     "焦虑水平有所下降，但在提及学校作业时仍表现出一定压力。",
         * --     "社交互动中的自信心仍然不足，需要更多的鼓励和支持。",
         * --     "与AI助手的互动频率增加，表明他对这种交流方式感到舒适。"
         * --   ], -- 关键发现列表（分析结果详情）
         * --   "recommendations": [
         * --     {
         * --       "title": "继续保持与小明的定期沟通",
         * --       "description": "每周安排1-2次简短的交流，关注他的日常感受和需求。",
         * --       "priority": "high" -- 优先级（影响展示排序）？？？
         * --     },
         * --     {
         * --       "title": "开展自信心提升活动",
         * --       "description": "设计一些小明擅长的活动，通过成功体验增强他的自信心。",
         * --       "priority": "medium"
         * --     },
         * --     {
         * --       "title": "与学校老师保持沟通",
         * --       "description": "了解小明在学校的表现，共同制定支持计划。"
         * --     },
         * --     {
         * --       "title": "提供情绪管理技巧指导",
         * --       "description": "教导小明一些简单的情绪调节方法，帮助他应对压力情境。"
         * --     }
         * --   ] -- 建议列表（分析结果详情->服务计划生成依据）
         * -- }
         *
         */

        //获取该儿童信息的聊天记录
        //根据id查询儿童信息
        Child child = childService.getById(id);
        if (child == null) {
            log("未找到ID为{}的儿童信息", id);
            return null;
        }
        //获取该儿童的聊天记录,上次分析时间
        List<ChatMessage> chatMessages = aiChatRecordService.getChatMessagesByChildId(id, child.getAiAnalysisTime());
        if (chatMessages.isEmpty()) {
            log("儿童ID为{}的聊天记录为空", id);
            return null;
        }
        //将之前的分结果也添加到提示词中
        String userPrompt = String.format(
                "儿童姓名：%s，儿童年龄：%d岁，聊天记录：%s，情感分析结果：%s",
                child.getName(), child.getAge(),
                String.join("\n", chatMessages.stream().map(msg -> msg.getRole() + ": " + msg.getContent()).collect(Collectors.toList())),
                child.getAiStructInfo() != null ? child.getAiStructInfo() : "暂无情感分析结果"
        );
        //发起AI分析请求
        ChatContent chatContent = arkUtil.botChat(ChatRequest.builder().prompt(userPrompt).history(chatMessages).build(),systemPrompt);
        
        // 保存分析结果到数据库,并更新儿童的分析时间和AI分析信息
        if (chatContent != null) {
            // 构建AiAnalysisLog实体
            AiAnalysisLog aiAnalysisLog = new AiAnalysisLog();
            aiAnalysisLog.setChildId(id);
            aiAnalysisLog.setAnalysisResult(chatContent.getContent()); // 使用AI分析结果
            aiAnalysisLog.setTriggerType("AUTO"); // 设置触发类型为自动
            aiAnalysisLog.setCreateTime(LocalDateTime.now());
            
            // 保存到数据库
            aiAnalysisLogMapper.insert(aiAnalysisLog);
            
            // 更新儿童的分析时间和AI分析信息
            child.setAiAnalysisTime(LocalDateTime.now());
            child.setAiStructInfo(chatContent.getContent()); // 将AI分析结果保存到儿童的结构化信息中
            childService.updateById(child);
            
            log("AI分析结果已保存，记录ID: {}", aiAnalysisLog.getId());
        }
        
        // 创建并返回AI分析结果VO
        AiAnalysisResultVO result = new AiAnalysisResultVO();
        result.setChildId(child.getId());
        result.setChildName(child.getName() + "(" + child.getAge() + "岁)");
        result.setCreateTime(LocalDateTime.now());
        result.setAiStructInfo(chatContent != null ? chatContent.getContent() : null);
        
        // 解析结构化信息
        if (chatContent != null) {
            parseStructuredInfo(chatContent.getContent(), result);
        }
        
        return result;
    }

    // 添加日志记录方法
    private void log(String message) {
        // 实际项目中应该使用日志框架
        System.out.println("[LOG] " + message);
    }
    /**
     * 记录日志，支持格式化输出
     * @param message 日志消息模板
     * @param args 格式化参数
     */
    private void log(String message, Object... args) {
        // 支持可变参数的重载方法
        System.out.println("[LOG] " + String.format(message, args));
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
            log("未找到ID为{}的儿童信息", id);
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