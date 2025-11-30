package com.caresync.ai;

import com.caresync.ai.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class TestJsonParsing {
    public static void main(String[] args) {
        // 使用JsonUtil中的ObjectMapper，确保配置一致
        ObjectMapper objectMapper = JsonUtil.getObjectMapper();
        
        // 模拟ai_suggestions JSON数据
        String aiSuggestionsJson = """
        {
          "target_suggest": [
            "降低孤独焦虑，建立积极心态",
            "增强情绪管理，正确表达感受",
            "提升社交能力，改善人际沟通"
          ],
          "measures_suggest": [
            {
              "week": "建立信任关系",
              "details": [
                {"content": "初次见面，了解小明的兴趣爱好和日常生活情况。", "status": "completed", "assist_track_log_id": 1},
                {"content": "一起参与小明感兴趣的活动（如绘画、下棋），建立初步信任。", "status": "completed", "assist_track_log_id": 2},
                {"content": "与小明约定每周固定的见面时间，增加安全性。", "status": "in_progress", "assist_track_log_id": 3}
              ]
            },
            {
              "week": "情绪识别与表达",
              "details": [
                {"content": "通过情绪卡片游戏，帮助小明识别不同的情绪。", "status": "pending", "assist_track_log_id": 4},
                {"content": "引导小明用绘画的方式表达自己的内心感受。", "status": "pending", "assist_track_log_id": 5},
                {"content": "教授简单的情绪调节方法，如深呼吸、倾诉等。", "status": "pending", "assist_track_log_id": 6}
              ]
            }
          ],
          "evaluation_index": {
            "emotional_stability": "良好",
            "social_interaction": "一般",
            "self_confidence": "需要提升"
          }
        }
        """;
        
        try {
            JsonNode jsonNode = objectMapper.readTree(aiSuggestionsJson);
            
            // 测试target_suggest解析
            if (jsonNode.has("target_suggest")) {
                List<String> targetSuggest = new ArrayList<>();
                JsonNode targetSuggestNode = jsonNode.get("target_suggest");
                if (targetSuggestNode.isArray()) {
                    for (JsonNode node : targetSuggestNode) {
                        targetSuggest.add(node.asText());
                    }
                }
                System.out.println("target_suggest: " + targetSuggest);
            }
            
            // 测试measures_suggest解析
            if (jsonNode.has("measures_suggest")) {
                List<Map<String, Object>> measuresSuggest = new ArrayList<>();
                JsonNode measuresSuggestNode = jsonNode.get("measures_suggest");
                if (measuresSuggestNode.isArray()) {
                    for (JsonNode measureNode : measuresSuggestNode) {
                        Map<String, Object> measure = new HashMap<>();
                        
                        if (measureNode.has("week")) {
                            measure.put("week", measureNode.get("week").asText());
                        }
                        
                        if (measureNode.has("details")) {
                            List<Map<String, Object>> details = new ArrayList<>();
                            JsonNode detailsNode = measureNode.get("details");
                            if (detailsNode.isArray()) {
                                for (JsonNode detailNode : detailsNode) {
                                    Map<String, Object> detail = new HashMap<>();
                                    
                                    if (detailNode.has("content")) {
                                        detail.put("content", detailNode.get("content").asText());
                                    }
                                    
                                    if (detailNode.has("status")) {
                                        detail.put("status", detailNode.get("status").asText());
                                    }
                                    
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
                System.out.println("measures_suggest: " + measuresSuggest);
            }
            
            // 测试evaluation_index解析
            if (jsonNode.has("evaluation_index")) {
                Map<String, Object> evaluationIndex = new HashMap<>();
                JsonNode evaluationIndexNode = jsonNode.get("evaluation_index");
                evaluationIndexNode.fields().forEachRemaining(entry -> {
                    evaluationIndex.put(entry.getKey(), entry.getValue());
                });
                System.out.println("evaluation_index: " + evaluationIndex);
            }
            
            System.out.println("JSON解析测试成功！");
            
        } catch (Exception e) {
            System.out.println("JSON解析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}