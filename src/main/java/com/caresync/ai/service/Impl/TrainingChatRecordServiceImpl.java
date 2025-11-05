package com.caresync.ai.service.Impl;

import com.caresync.ai.model.entity.TrainingChatRecord;
import com.caresync.ai.mapper.TrainingChatRecordMapper;
import com.caresync.ai.service.ITrainingChatRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-04
 */
@Service
public class TrainingChatRecordServiceImpl extends ServiceImpl<TrainingChatRecordMapper, TrainingChatRecord> implements ITrainingChatRecordService {
    @Autowired
    private TrainingChatRecordMapper trainingChatRecordMapper;

    @Override
    public void roundNumAdd(Long sessionId) {
        TrainingChatRecord trainingChatRecord = trainingChatRecordMapper.selectById(sessionId);
        trainingChatRecord.setRoundNum(trainingChatRecord.getRoundNum() + 1);
        trainingChatRecordMapper.updateById(trainingChatRecord);
    }
}
