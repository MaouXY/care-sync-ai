package com.caresync.ai.livetalking;

import com.caresync.ai.model.DTO.ChatMessageDTO;
import com.caresync.ai.model.VO.ChildChatMessageVO;
import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.utils.ArkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * 接收儿童端发送的消息，转发给直播互动类
 */
@Slf4j
@Service
public class LiveTalkingService {

    @Autowired
    private ArkUtil arkUtil;

    // http://localhost:8010/
    @Value("${liveTalking.url}")
    private String liveTalkingUrl;

    /**
     * 发送聊天消息
     *
     * @param chatMessageDTO 聊天消息DTO
     */
    public ChildChatMessageVO sendChatMessage(ChatMessageDTO chatMessageDTO) throws Exception {
        log.info("获取到的历史消息: {}", chatMessageDTO.getChatRequest().getHistory());
        log.info("获取到的用户消息: {}", chatMessageDTO.getChatRequest().getPrompt());

        // 儿童端-系统提示词
        String systemPrompt = "你是一个陪伴儿童的智能ai，你需要用中文回答儿童的问题。";
        //调用LLM生成回复
        ChatContent chatContent = arkUtil.botChat(chatMessageDTO.getChatRequest(), systemPrompt);
        log.info("LLM回复: {}", chatContent.getContent());

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
}