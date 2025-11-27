package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.model.DTO.StartSessionDTO;
import com.caresync.ai.model.VO.ChildChatMessageVO;
import com.caresync.ai.model.VO.ChildSessionVO;
import com.caresync.ai.model.ai.ChatMessage;
import com.caresync.ai.model.entity.AiChatRecord;
import com.caresync.ai.mapper.AiChatRecordMapper;
import com.caresync.ai.service.IAiChatRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Service
public class AiChatRecordServiceImpl extends ServiceImpl<AiChatRecordMapper, AiChatRecord> implements IAiChatRecordService {
    /**
     * 获取儿童的聊天记录
     *
     * @param childId          儿童ID
     * @param lastAnalysisTime 上次分析时间
     * @return 聊天记录VO列表
     */
    @Override
    public List<ChatMessage> getChatMessagesByChildId(Long childId, LocalDateTime lastAnalysisTime) {
        //从数据库中获取lastAnalysisTime-now的聊天记录
        List<AiChatRecord> chatRecords = baseMapper.selectList(
                new LambdaQueryWrapper<AiChatRecord>()
                        .eq(AiChatRecord::getChildId, childId)
                        .gt(AiChatRecord::getCreateTime, lastAnalysisTime)
                        .orderByAsc(AiChatRecord::getCreateTime)
        );
        // 转换为VO列表
        return chatRecords.stream()
                .map(record -> new ChatMessage(record.getAiReply() ? "ai" : "user", record.getContent()))
                .collect(Collectors.toList());
    }
}