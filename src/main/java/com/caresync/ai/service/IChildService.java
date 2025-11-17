package com.caresync.ai.service;

import com.caresync.ai.model.DTO.ChildLoginDTO;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.CreateChildDTO;
import com.caresync.ai.model.DTO.UpdateChildInfoDTO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.ChildVO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  儿童服务接口
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
public interface IChildService extends IService<Child> {

    /**
     * 儿童登录
     * @param childLoginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(ChildLoginDTO childLoginDTO);

     /**
     * 获取儿童个人信息
     * @param id 儿童ID
     * @return 儿童信息VO
     */
    ChildVO getChild(Long id);

    /**
     * 获取儿童个人信息
     * @param id 儿童ID
     * @return 儿童信息
     */
    ChildInfoVO getChildInfo(Long id);

    /**
     * 更新儿童个人信息
     * @param updateChildInfoDTO 更新信息
     */
    void updateChildInfo(UpdateChildInfoDTO updateChildInfoDTO);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取儿童列表
     * @param childQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ChildQueueVO> getChildList(ChildQueryDTO childQueryDTO);

    /**
     * 创建儿童账号
     * @param createChildDTO 创建儿童账号DTO
     * @return 创建的儿童ID
     */
    Long createChild(CreateChildDTO createChildDTO);
}