package com.caresync.ai.livetalking; 
 
 import com.caresync.ai.model.DTO.ChatMessageDTO; 
 import com.caresync.ai.model.VO.ChildChatMessageVO; 
 import com.caresync.ai.model.ai.ChatContent; 
 import com.caresync.ai.utils.ArkUtil;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;

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

     @Value("${liveTalking.url}")
     private String liveTalkingUrl;

     /**
      * 发送聊天消息
      *
      * @param chatMessageDTO 聊天消息DTO
      */
     public ChildChatMessageVO sendChatMessage(ChatMessageDTO chatMessageDTO) throws Exception {
         // 系统提示词
         String systemPrompt = "你是一个儿童，你需要用中文回答儿童的问题。";
         //调用LLM生成回复
         ChatContent chatContent = arkUtil.botChat(chatMessageDTO.getChatRequest(),systemPrompt);
         log.info("LLM回复: {}", chatContent.getContent());

         //发送HTTP请求给LiveTalking服务，LLM->TS->wav2lip->直播互动类 ，url为"/human"
         // 创建URL对象
         URL url = new URL(liveTalkingUrl+"human");
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();

         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setDoOutput(true);
         // 构建请求体
         String requestBody = "{"+
                 "\"sessionid\": " + chatMessageDTO.getSessionId() + "," +
                 "\"interrupt\": true," +
                 "\"type\": \"echo\"," +
                 "\"text\": \"" + chatContent.getContent() + "\"}";

         try (OutputStream os = connection.getOutputStream()) {
             byte[] input = requestBody.getBytes("utf-8");
             os.write(input, 0, input.length);
         }catch (Exception e){
             log.error("发送聊天消息失败: {}", e.getMessage());
             throw e;
         }

         //关闭连接
         connection.disconnect();
         //返回LLM生成的回复
         return ChildChatMessageVO.builder()
                 .content(chatContent.getContent())
                 .timestamp(LocalDateTime.now())
                 .build();
     }
}