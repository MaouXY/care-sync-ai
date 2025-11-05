package com.caresync.ai.service;

import com.caresync.ai.model.DTO.AiAnalysisQueryDTO;
import com.caresync.ai.model.VO.AiAnalysisResultVO;
import com.caresync.ai.model.entity.AiAnalysisLog;
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
public interface IAiAnalysisLogService extends IService<AiAnalysisLog> {

    /**
     * 获取AI分析结果列表
     * @param aiAnalysisQueryDTO 查询条件，包含儿童姓名、潜在问题、情感趋势等查询参数（均允许空值）
     * @return 分页结果
     */
    PageResult<AiAnalysisResultVO> getAiAnalysisResults(AiAnalysisQueryDTO aiAnalysisQueryDTO);

    /**
     * 获取AI分析结果详情
     * @param id 儿童ID
     * @return AI分析结果VO
     */
    AiAnalysisResultVO getAiAnalysisDetail(Long id);

}