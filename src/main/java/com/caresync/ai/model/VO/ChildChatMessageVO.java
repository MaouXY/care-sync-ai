package com.caresync.ai.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 儿童聊天消息响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChildChatMessageVO implements Serializable {
    /**
     * 聊天内容
     */
    private String content;
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
