package com.caresync.ai.service;

import com.caresync.ai.model.DTO.SocialWorkerLoginDTO;
import com.caresync.ai.model.DTO.UpdateSocialWorkerInfoDTO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.entity.SocialWorker;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  社工服务接口
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
public interface ISocialWorkerService extends IService<SocialWorker> {
    
    /**
     * 社工登录
     * @param socialWorkerLoginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(SocialWorkerLoginDTO socialWorkerLoginDTO);
    
    /**
     * 获取社工个人信息
     * @param id 社工ID
     * @return 社工信息
     */
    SocialWorkerInfoVO getSocialWorkerInfo(Long id);
    
    /**
     * 更新社工个人信息
     * @param updateSocialWorkerInfoDTO 更新信息
     */
    void updateSocialWorkerInfo(UpdateSocialWorkerInfoDTO updateSocialWorkerInfoDTO);
    
    /**
     * 登出
     */
    void logout();
}