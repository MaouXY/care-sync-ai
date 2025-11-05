package com.caresync.ai.utils;

import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArkUtil {
    @Autowired
    private ArkService arkService;

    @Value("${big.model.api.model}")
    private String botId;

    // TODO 区分ai，因为需要使用不同的prompts
    public ChatContent botChat(ChatRequest request,String systemPrompt) {
        System.out.println(LocalDate.now() + " " + request.toString());

        // 创建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        // 添加系统消息
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(systemPrompt)
                .build();
        messages.add(systemMessage);

        // 添加历史消息
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            // 必须自己自定，命名冲突，笑哭
            for (com.caresync.ai.model.ai.ChatMessage historyMsg : request.getHistory()) {
                // 需要将项目中的ChatMessage转换为火山引擎SDK的ChatMessage
                ChatMessageRole role = "user".equals(historyMsg.getRole()) ? ChatMessageRole.USER : ChatMessageRole.ASSISTANT;
                ChatMessage chatMessage = ChatMessage.builder()
                        .role(role)
                        .content(historyMsg.getContent())
                        .build();
                messages.add(chatMessage);
            }
        }

        // 添加当前用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(request.getPrompt())
                .build();
        messages.add(userMessage);

        // 创建Bot聊天完成请求
        BotChatCompletionRequest botRequest = BotChatCompletionRequest.builder()
                .botId(botId) // 指定Bot ID
                .messages(messages) // 设置消息列表
                .build();

        // 调用模型服务
        BotChatCompletionResult result = arkService.createBotChatCompletion(botRequest);

        // 处理模型返回结果
        ChatContent chatContent = new ChatContent();

        // 获取生成的内容
        String content = result.getChoices().get(0).getMessage().getContent().toString();
        // 处理生成的内容,兼容前端显示
        //content = content.replaceAll("\\n", "<br>");
        chatContent.setContent(content);

        // 对于R1模型，可以获取reasoning content
        String reasoningContent = result.getChoices().get(0).getMessage().getReasoningContent();
        if (reasoningContent != null && !reasoningContent.isEmpty()) {
            //content = "</think>" + reasoningContent + "\n</think>\n" + content;
            chatContent.setReasoningContent(reasoningContent);
        }

        // 如果有参考文献，可以添加到返回结果中
        if (result.getReferences() != null && !result.getReferences().isEmpty()) {
            StringBuilder references = new StringBuilder("\n\n参考文献:\n");
            result.getReferences().forEach(ref -> references.append(ref.getUrl()).append("\n"));
        }

        System.out.println(chatContent.toString());
        return chatContent;

        //快速测试用
        //return Result.success("success", new ChatContent("这是一段AI快速测试对话返回信息，并非由ai生成。",""));
    }
}
