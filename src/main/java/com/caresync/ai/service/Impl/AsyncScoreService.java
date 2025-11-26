package com.caresync.ai.service.Impl;

import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatMessage;
import com.caresync.ai.model.ai.ChatRequest;
import com.caresync.ai.utils.ArkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步评分服务类
 */
@Slf4j
@Service
public class AsyncScoreService {

    @Autowired
    private ArkUtil arkUtil;

    /**
     * 异步获取AI评分
     */
    @Async("aiScoreExecutor")
    public CompletableFuture<BigDecimal> getScoreFromAiAsync(String prompt, List<ChatMessage> history) {
        try {
            String systemPrompt = "你是一个专业的社工，负责为其他社工提供评分。";

            ChatRequest request = ChatRequest.builder()
                    .prompt(prompt)
                    .history(history)
                    .build();

            ChatContent content = arkUtil.botChat(request, systemPrompt);
            log.info("AI评分: {}", content != null ? content.getContent() : "null");

            if (content == null || content.getContent() == null) {
                log.warn("AI评分内容为空，使用默认分数");
                return CompletableFuture.completedFuture(BigDecimal.ZERO);
            }
            String scoreStr = content.getContent().trim();
            // 提取数字
            scoreStr = scoreStr.replaceAll("[^0-9.]", "");
            if (scoreStr.isEmpty()) {
                log.warn("无法从AI响应中提取分数，使用默认分数");
                return CompletableFuture.completedFuture(BigDecimal.ZERO);
            }
            return CompletableFuture.completedFuture(new BigDecimal(scoreStr));
        } catch (Exception e) {
            log.error("获取AI评分失败: {}", e.getMessage());
            return CompletableFuture.completedFuture(BigDecimal.ZERO);
        }
    }

    /**
     * 异步获取AI综合评价
     */
    @Async("aiScoreExecutor")
    public CompletableFuture<ChatContent> getComprehensiveCommentFromAiAsync(String prompt, List<ChatMessage> history) {
        try {
            String systemPrompt = "你是一个专业的社工，负责为其他社工提供综合评价。";

            ChatRequest request = ChatRequest.builder()
                    .prompt(prompt)
                    .history(history)
                    .build();
            
            ChatContent chatContent = arkUtil.botChat(request, systemPrompt);
            log.info("AI综合评价请求: {}", chatContent != null ? chatContent.getContent() : "null");

            return CompletableFuture.completedFuture(chatContent);
        } catch (Exception e) {
            log.error("获取AI综合评价失败: {}", e.getMessage());
            // 创建默认的评价内容，避免空指针异常
            ChatContent defaultContent = new ChatContent();
            defaultContent.setContent("该会话未生成综合评价，可能是由于AI服务不可用或会话内容不足。");
            return CompletableFuture.completedFuture(defaultContent);
        }
    }
}