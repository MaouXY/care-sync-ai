package com.caresync.ai.livetalking;

import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.model.DTO.ChatMessageDTO;
import com.caresync.ai.model.VO.ChildChatMessageVO;
import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatRequest;
import com.caresync.ai.model.entity.AiChatRecord;
import com.caresync.ai.service.IAiChatRecordService;
import com.caresync.ai.utils.ArkUtil;
import com.caresync.ai.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 接收儿童端发送的消息，转发给直播互动类
 */
@Slf4j
@Service
public class LiveTalkingService {

    @Autowired
    private ArkUtil arkUtil;

    @Autowired
    private IAiChatRecordService aiChatRecordService;

    @Autowired
    private JwtConfig jwtConfig;

    @Value("${liveTalking.url}")
    private String liveTalkingUrl;

    @Value("${big.model.api.model}")
    private String botId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送聊天消息
     *
     * @param chatMessageDTO 聊天消息DTO
     */
    public ChildChatMessageVO sendChatMessage(ChatMessageDTO chatMessageDTO) throws Exception {
        log.info("获取到的历史消息: {}", chatMessageDTO.getChatRequest().getHistory());
        log.info("获取到的用户消息: {}", chatMessageDTO.getChatRequest().getPrompt());

        // 获取当前登录儿童的ID
        Long childId = BaseContext.getCurrentId();
        if (childId == null) {
            log.error("未获取到当前登录儿童信息");
            throw new RuntimeException("未登录，请先登录");
        }
        log.info("当前登录儿童ID: {}", childId);

        // 获取会话ID和数字人会话ID
        String sessionId = chatMessageDTO.getSessionId();
        String digiSessionId = chatMessageDTO.getDigiSessionId() != null ? chatMessageDTO.getDigiSessionId().toString() : null;

        // 查询当前会话的最大轮次
        Integer maxRound = getMaxRoundBySessionId(sessionId);
        int roundNum = maxRound == null ? 1 : maxRound + 1;
        log.info("当前会话轮次: {}", roundNum);

        // 保存儿童发送的消息到ai_chat_record表
        saveChildMessage(childId, sessionId, digiSessionId, roundNum, chatMessageDTO);

        // 儿童端-系统提示词
        String systemPrompt = "你是一个陪伴儿童的智能ai，你需要用中文回答儿童的问题。";
        //调用LLM生成回复
        ChatContent chatContent = arkUtil.botChat(chatMessageDTO.getChatRequest(), systemPrompt);
        log.info("LLM回复: {}", chatContent.getContent());

        // 保存AI回复的消息到ai_chat_record表
        saveAiReplyMessage(childId, sessionId, digiSessionId, roundNum, chatContent.getContent());

        log.info("数字人会话ID: {}", chatMessageDTO.getDigiSessionId());

        if (chatMessageDTO.getDigiSessionId() != null) {//为null则是文本对话，非null为数字人对话

            // 构建完整的URL
            String fullUrl = liveTalkingUrl + (liveTalkingUrl.endsWith("/") ? "human" : "/human");
            log.info("发送请求到LiveTalking服务URL: {}", fullUrl);

            // 创建URL对象
            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时

            // 构建请求体 - 修复JSON格式问题
            String requestBody = buildRequestBody(chatContent.getContent(), chatMessageDTO.getDigiSessionId().toString());
            log.info("发送给LiveTalking服务的请求体: {}", requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
            } catch (Exception e) {
                log.error("发送聊天消息失败: {}", e.getMessage());
                throw e;
            }

            // 检查HTTP响应状态
            int responseCode = connection.getResponseCode();
            log.info("LiveTalking服务响应状态码: {}", responseCode);

            if (responseCode != 200) {
                // 读取错误响应
                String errorResponse = readErrorResponse(connection);
                log.error("LiveTalking服务错误响应: {}", errorResponse);
                throw new RuntimeException("LiveTalking服务返回错误状态码: " + responseCode + ", 错误信息: " + errorResponse);
            } else {
                // 读取成功响应
                String successResponse = readSuccessResponse(connection);
                log.info("LiveTalking服务成功响应: {}", successResponse);
            }

            // 关闭连接
            connection.disconnect();
        }

        // 返回LLM生成的回复
        return ChildChatMessageVO.builder()
                .content(chatContent.getContent())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 发送聊天消息（流式接口）
     * @param chatMessageDTO 聊天消息DTO
     * @param authorization JWT令牌（token）
     * @return SSE流式响应
     */
    public SseEmitter sendChatMessageStream(ChatMessageDTO chatMessageDTO, String authorization) {
        // 创建SseEmitter，设置超时时间为5分钟
        SseEmitter sseEmitter = new SseEmitter(300000L);

        // 异步处理，避免阻塞主线程
        new Thread(() -> {
            try {
                log.info("流式聊天开始，获取到的用户消息: {}", chatMessageDTO.getChatRequest().getPrompt());

                // 从JWT令牌中获取用户ID
                Long childId = getUserIdFromToken(authorization);
                if (childId == null) {
                    log.error("未获取到当前登录儿童信息，token无效或已过期");
                    sseEmitter.send(SseEmitter.event().name("error").data("未登录，请先登录"));
                    sseEmitter.completeWithError(new RuntimeException("未登录，请先登录"));
                    return;
                }
                log.info("当前登录儿童ID: {}", childId);

                // 获取会话ID和数字人会话ID
                String sessionId = chatMessageDTO.getSessionId();
                String digiSessionId = chatMessageDTO.getDigiSessionId() != null ? chatMessageDTO.getDigiSessionId().toString() : null;

                // 查询当前会话的最大轮次
                Integer maxRound = getMaxRoundBySessionId(sessionId);
                int roundNum = maxRound == null ? 1 : maxRound + 1;
                log.info("当前会话轮次: {}", roundNum);

                // 保存儿童发送的消息到ai_chat_record表
                saveChildMessage(childId, sessionId, digiSessionId, roundNum, chatMessageDTO);

                // 儿童端-系统提示词
                String systemPrompt = "你是一个陪伴儿童的智能ai，你需要用中文回答儿童的问题。";

                // 用于累加完整回复
                StringBuilder fullResponse = new StringBuilder();

                // 调用ArkUtil的流式方法获取回复
                Flux<String> responseFlux = arkUtil.streamBotChat(chatMessageDTO.getChatRequest(), systemPrompt);

                // 订阅流式响应
                responseFlux
                        .doOnError(throwable -> {
                            log.error("AI流式服务错误: {}", throwable.getMessage());
                            try {
                                // 构造错误信息的JSON格式
                                Map<String, Object> errorChunk = new HashMap<>();
                                errorChunk.put("id", "chatcmpl-" + UUID.randomUUID());
                                errorChunk.put("object", "chat.completion.chunk");
                                errorChunk.put("created", System.currentTimeMillis());
                                errorChunk.put("model", botId);

                                Map<String, Object> choice = new HashMap<>();
                                choice.put("index", 0);

                                Map<String, Object> delta = new HashMap<>();
                                delta.put("content", "AI服务错误: " + throwable.getMessage());
                                choice.put("delta", delta);
                                choice.put("finish_reason", null);

                                errorChunk.put("choices", new Object[]{choice});

                                String errorJson = objectMapper.writeValueAsString(errorChunk);
                                sseEmitter.send("data: " + errorJson + "\n\n");
                                sseEmitter.completeWithError(throwable);
                            } catch (Exception e) {
                                log.error("发送错误信息失败: {}", e.getMessage());
                            }
                        })
                        .doFinally(signalType -> {
                            try {
                                // 保存完整回复到数据库
                                if (fullResponse.length() > 0) {
                                    saveAiReplyMessage(childId, sessionId, digiSessionId, roundNum, fullResponse.toString());

                                    // 如果数字人会话存在，发送给LiveTalking服务
                                    if (chatMessageDTO.getDigiSessionId() != null) {
                                        sendToLiveTalkingService(fullResponse.toString(), chatMessageDTO.getDigiSessionId().toString());
                                    }
                                }
                            } catch (Exception e) {
                                log.error("保存AI回复消息异常: {}", e.getMessage());
                            } finally {
                                try {
                                    // 无论是否发生异常，都发送流结束信号
                                    sseEmitter.send("data: [DONE]\n\n");
                                    sseEmitter.complete();
                                } catch (Exception e) {
                                    log.error("发送流结束信号失败: {}", e.getMessage());
                                }
                            }
                        })
                        .subscribe(content -> {
                            try {
                                if (!content.isEmpty()) {
                                    // 累加完整回复
                                    fullResponse.append(content);

                                    // 构造符合OpenAI格式的JSON数据
                                    Map<String, Object> chatChunk = new HashMap<>();
                                    chatChunk.put("id", "chatcmpl-" + UUID.randomUUID());
                                    chatChunk.put("object", "chat.completion.chunk");
                                    chatChunk.put("created", System.currentTimeMillis());
                                    chatChunk.put("model", botId);

                                    // 构造choices数组
                                    Map<String, Object> choice = new HashMap<>();
                                    choice.put("index", 0);

                                    // 构造delta对象（增量内容）
                                    Map<String, Object> delta = new HashMap<>();
                                    delta.put("content", content);
                                    choice.put("delta", delta);
                                    choice.put("finish_reason", null);

                                    chatChunk.put("choices", new Object[]{choice});

                                    // 转换为JSON字符串
                                    String json = objectMapper.writeValueAsString(chatChunk);
                                    // 推送当前片段到前端，使用标准SSE格式
                                    sseEmitter.send("data: " + json + "\n\n");
                                }
                            } catch (JsonProcessingException e) {
                                log.error("JSON序列化失败: {}", e.getMessage());
                            } catch (Exception e) {
                                log.error("发送流式数据失败: {}", e.getMessage());
                                try {
                                    sseEmitter.completeWithError(e);
                                } catch (Exception ex) {
                                    log.error("关闭SSE连接失败: {}", ex.getMessage());
                                }
                            }
                        });

            } catch (Exception e) {
                log.error("流式聊天处理异常: {}", e.getMessage());
                try {
                    sseEmitter.send(SseEmitter.event().name("error").data("服务错误: " + e.getMessage()));
                    sseEmitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误信息失败: {}", ex.getMessage());
                }
            }
        }).start();

        return sseEmitter;
    }

    /**
     * 从JWT令牌中获取用户ID
     * @param authorization 授权头（token）
     * @return 用户ID，如果token无效返回null
     */
    private Long getUserIdFromToken(String authorization) {
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//            log.error("Authorization头格式不正确: {}", authorization);
//            return null;
//        }

        try {
//            // 提取token（移除"Bearer "前缀）
//            String token = authorization.substring(7);

            // 解析JWT令牌
            Claims claims = JwtUtil.parseJWT(jwtConfig.getSecret(), authorization);

            // 从claims中获取用户ID
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("从JWT令牌解析的用户ID: {}", userId);

            return userId;
        } catch (JwtException e) {
            log.error("JWT令牌解析失败: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("获取用户ID异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送消息给LiveTalking服务
     */
    private void sendToLiveTalkingService(String content, String sessionId) {
        try {
            // 构建完整的URL
            String fullUrl = liveTalkingUrl + (liveTalkingUrl.endsWith("/") ? "human" : "/human");
            log.info("发送请求到LiveTalking服务URL: {}", fullUrl);

            // 创建URL对象
            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时

            // 构建请求体
            String requestBody = buildRequestBody(content, sessionId);
            log.info("发送给LiveTalking服务的请求体: {}", requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
            } catch (Exception e) {
                log.error("发送聊天消息失败: {}", e.getMessage());
                return;
            }

            // 检查HTTP响应状态
            int responseCode = connection.getResponseCode();
            log.info("LiveTalking服务响应状态码: {}", responseCode);

            if (responseCode != 200) {
                // 读取错误响应
                String errorResponse = readErrorResponse(connection);
                log.error("LiveTalking服务错误响应: {}", errorResponse);
            } else {
                // 读取成功响应
                String successResponse = readSuccessResponse(connection);
                log.info("LiveTalking服务成功响应: {}", successResponse);
            }

            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            log.error("发送消息给LiveTalking服务失败: {}", e.getMessage());
        }
    }

    /**
     * 构建请求体 - 修复JSON格式问题
     */
    private String buildRequestBody(String content, String sessionId) {
        // 转义内容中的特殊字符
        String escapedContent = content
                .replace("\\", "\\\\")  // 转义反斜杠
                .replace("\"", "\\\"")   // 转义双引号
                .replace("\n", "\\n")    // 转义换行符
                .replace("\r", "\\r")    // 转义回车符
                .replace("\t", "\\t");   // 转义制表符

        return String.format(
                "{\"type\":\"echo\",\"text\":\"%s\",\"interrupt\":true,\"sessionid\":%s}",
                escapedContent,
                sessionId
        );
    }

    /**
     * 读取错误响应
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                byte[] errorBytes = errorStream.readAllBytes();
                return new String(errorBytes, "utf-8");
            }
        } catch (Exception e) {
            log.error("读取错误响应失败: {}", e.getMessage());
        }
        return "无法读取错误响应";
    }

    /**
     * 读取成功响应
     */
    private String readSuccessResponse(HttpURLConnection connection) {
        try {
            InputStream inputStream = connection.getInputStream();
            byte[] successBytes = inputStream.readAllBytes();
            return new String(successBytes, "utf-8");
        } catch (Exception e) {
            log.error("读取成功响应失败: {}", e.getMessage());
        }
        return "无法读取成功响应";
    }

    /**
     * 测试LiveTalking服务连接
     */
    public boolean testConnection() {
        try {
            String testUrl = liveTalkingUrl + (liveTalkingUrl.endsWith("/") ? "" : "/");
            URL url = new URL(testUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            log.info("LiveTalking服务连接测试结果: {}", responseCode == 200 ? "成功" : "失败");
            return responseCode == 200;
        } catch (Exception e) {
            log.error("LiveTalking服务连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 查询当前会话的最大轮次
     */
    private Integer getMaxRoundBySessionId(String sessionId) {
        try {
            // 查询当前会话的最大轮次
            List<AiChatRecord> records = aiChatRecordService.lambdaQuery()
                    .eq(AiChatRecord::getSessionId, sessionId)
                    .orderByDesc(AiChatRecord::getRoundNum)
                    .last("LIMIT 1")
                    .list();

            if (records != null && !records.isEmpty()) {
                return records.get(0).getRoundNum();
            }
            return null;
        } catch (Exception e) {
            log.error("查询会话最大轮次失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存儿童发送的消息
     */
    private void saveChildMessage(Long childId, String sessionId, String digiSessionId, int roundNum, ChatMessageDTO chatMessageDTO) {
        try {
            // 确保contentType符合数据库约束要求（必须是大写的"TEXT"或"VOICE"）
            String contentType = chatMessageDTO.getContentType();
            if (contentType != null) {
                contentType = contentType.toUpperCase();
                if (!"TEXT".equals(contentType) && !"VOICE".equals(contentType)) {
                    contentType = "TEXT"; // 默认值
                }
            } else {
                contentType = "TEXT"; // 默认值
            }

            AiChatRecord childRecord = AiChatRecord.builder()
                    .childId(childId)
                    .sessionId(sessionId)
                    .digiSessionId(digiSessionId != null ? digiSessionId : "")
                    .roundNum(roundNum)
                    .contentType(contentType)
                    .content(chatMessageDTO.getChatRequest().getPrompt())
                    .aiReply(false) // 儿童消息
                    .filtered(false) // 默认未过滤
                    .emotionTag(null) // 情感标签（可后续分析）
                    .build();

            boolean saved = aiChatRecordService.save(childRecord);
            if (saved) {
                log.info("保存儿童消息成功，记录ID: {}", childRecord.getId());
            } else {
                log.error("保存儿童消息失败");
            }
        } catch (Exception e) {
            log.error("保存儿童消息异常: {}", e.getMessage());
        }
    }

    /**
     * 保存AI回复的消息
     */
    private void saveAiReplyMessage(Long childId, String sessionId, String digiSessionId, int roundNum, String aiContent) {
        try {
            AiChatRecord aiRecord = AiChatRecord.builder()
                    .childId(childId)
                    .sessionId(sessionId)
                    .digiSessionId(digiSessionId != null ? digiSessionId : "")
                    .roundNum(roundNum)
                    .contentType("TEXT") // AI回复都是文本
                    .content(aiContent)
                    .aiReply(true) // AI回复
                    .filtered(false) // 默认未过滤
                    .emotionTag(null) // 情感标签（可后续分析）
                    .build();

            boolean saved = aiChatRecordService.save(aiRecord);
            if (saved) {
                log.info("保存AI回复消息成功，记录ID: {}", aiRecord.getId());
            } else {
                log.error("保存AI回复消息失败");
            }
        } catch (Exception e) {
            log.error("保存AI回复消息异常: {}", e.getMessage());
        }
    }
}
