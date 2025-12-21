package com.caresync.ai.model.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 训练对话流式响应VO
 */
@Data
@Schema(description = "训练对话流式响应VO")
public class TrainingChatStreamResponseVO {
    
    @Schema(description = "响应类型")
    private String type; // "child_reply", "emotion_analysis", "ai_guidance", "done"
    
    @Schema(description = "响应内容")
    private String content;
    
    @Schema(description = "会话ID")
    private Long sessionId;
    
    @Schema(description = "时间戳")
    private String timestamp;
    
    @Schema(description = "是否完成")
    private Boolean isCompleted;
    
    public TrainingChatStreamResponseVO() {
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.isCompleted = false;
    }
    
    public static TrainingChatStreamResponseVO createChildReply(String content, Long sessionId) {
        TrainingChatStreamResponseVO vo = new TrainingChatStreamResponseVO();
        vo.setType("child_reply");
        vo.setContent(content);
        vo.setSessionId(sessionId);
        return vo;
    }
    
    public static TrainingChatStreamResponseVO createEmotionAnalysis(String content, Long sessionId) {
        TrainingChatStreamResponseVO vo = new TrainingChatStreamResponseVO();
        vo.setType("emotion_analysis");
        vo.setContent(content);
        vo.setSessionId(sessionId);
        return vo;
    }
    
    public static TrainingChatStreamResponseVO createAiGuidance(String content, Long sessionId) {
        TrainingChatStreamResponseVO vo = new TrainingChatStreamResponseVO();
        vo.setType("ai_guidance");
        vo.setContent(content);
        vo.setSessionId(sessionId);
        return vo;
    }
    
    public static TrainingChatStreamResponseVO createDone(Long sessionId) {
        TrainingChatStreamResponseVO vo = new TrainingChatStreamResponseVO();
        vo.setType("done");
        vo.setContent("");
        vo.setSessionId(sessionId);
        vo.setIsCompleted(true);
        return vo;
    }
}