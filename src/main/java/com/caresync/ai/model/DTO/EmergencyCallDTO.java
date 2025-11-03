package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 紧急呼叫请求参数
 */
@Data
public class EmergencyCallDTO {
    private String reason; // 紧急呼叫原因
}