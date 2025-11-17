package com.caresync.ai.service;

import com.caresync.ai.model.DTO.SchemeLogDTO;
import com.caresync.ai.model.DTO.SchemeLogQueryDTO;
import com.caresync.ai.model.VO.AssistSchemeLogVO;
import com.caresync.ai.model.VO.TaskStatisticsVO;
import com.caresync.ai.model.entity.AssistTrackLog;
import com.caresync.ai.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
public interface IAssistTrackLogService extends IService<AssistTrackLog> {

    /**
     * 获取服务方案日志列表（排除DRAFT状态）
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AssistSchemeLogVO> getSchemeList(SchemeLogQueryDTO queryDTO);

    /**
     * 获取服务方案日志（排除DRAFT状态）
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    AssistSchemeLogVO getScheme(SchemeLogDTO queryDTO);

//    /**
//     * 获取任务统计信息
//     * @param schemeId 方案ID
//     * @return 任务统计信息
//     */
//    TaskStatisticsVO getTaskStatistics(Long schemeId);
}