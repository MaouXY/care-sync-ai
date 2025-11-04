package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.model.DTO.AiAnalysisQueryDTO;
import com.caresync.ai.model.VO.AiAnalysisResultVO;
import com.caresync.ai.model.entity.AiAnalysisLog;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.AiAnalysisLogMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.IAiAnalysisLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // 添加日志记录方法
    private void log(String message) {
        // 实际项目中应该使用日志框架
        System.out.println("[LOG] " + message);
    }

    private void log(String message, Object... args) {
        // 支持可变参数的重载方法
        System.out.println("[LOG] " + String.format(message, args));
    }

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