package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 结束训练会话请求参数
 */
@Data
public class EndTrainingSessionDTO {
    private Long sessionId; // 训练会话ID
}

