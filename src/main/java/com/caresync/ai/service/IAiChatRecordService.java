package com.caresync.ai.service;

import com.caresync.ai.model.DTO.StartSessionDTO;
import com.caresync.ai.model.VO.ChildChatMessageVO;
import com.caresync.ai.model.VO.ChildSessionVO;
import com.caresync.ai.model.VO.SessionVO;
import com.caresync.ai.model.ai.ChatMessage;
import com.caresync.ai.model.entity.AiChatRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
public interface IAiChatRecordService extends IService<AiChatRecord> {

    /**
     * 获取儿童的聊天记录
     * @param childId 儿童ID
     * @param lastAnalysisTime 上次分析时间
     * @return 聊天记录VO列表
     */
    List<ChatMessage> getChatMessagesByChildId(Long childId, LocalDateTime lastAnalysisTime);
}
