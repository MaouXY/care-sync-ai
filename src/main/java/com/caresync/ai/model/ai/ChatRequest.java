package com.caresync.ai.model.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequest {
    private String prompt; // 用户当前的问题
    private List<ChatMessage> history; // 历史对话列表
}
