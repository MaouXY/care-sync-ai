package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表响应结果
 */
@Data
public class SessionVO implements Serializable {
    private String sessionId;
    private String digiSessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer messageCount;
    private String lastMessageContent;
}