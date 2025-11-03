package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息响应结果
 */
@Data
public class ChatMessageVO implements Serializable {
    private Long id;
    private Long childId;
    private String sessionId;
    private String digiSessionId;
    private Integer roundNum;
    private String contentType;
    private String content;
    private Boolean isAiReply;
    private Boolean isFiltered;
    private String emotionTag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}