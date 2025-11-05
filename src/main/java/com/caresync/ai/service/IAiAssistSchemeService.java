package com.caresync.ai.service;

import com.caresync.ai.model.DTO.SchemeQueryDTO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.model.entity.AiAssistScheme;
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
public interface IAiAssistSchemeService extends IService<AiAssistScheme> {

    /**
     * 获取服务方案列表
     * @param schemeQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AssistSchemeVO> getSchemeList(SchemeQueryDTO schemeQueryDTO);

    /**
     * 获取服务方案列表（管理视角）
     * @param schemeQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AssistSchemeVO> getSchemeListManage(SchemeQueryDTO schemeQueryDTO);

    /**
     * 获取服务方案详情
     * @param id 方案ID
     * @return 服务方案详情VO
     */
    DetailSchemeVO getSchemeDetail(Long id);
}