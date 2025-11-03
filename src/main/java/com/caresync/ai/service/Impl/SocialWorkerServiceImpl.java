package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.CodeConstant;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.constant.MessageConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.exception.BusinessException;
import com.caresync.ai.model.DTO.SocialWorkerLoginDTO;
import com.caresync.ai.model.DTO.UpdateSocialWorkerInfoDTO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.VO.SocialWorkerInfoVO;
import com.caresync.ai.model.entity.SocialWorker;
import com.caresync.ai.mapper.SocialWorkerMapper;
import com.caresync.ai.service.ISocialWorkerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.JwtUtil;
import com.caresync.ai.utils.PasswordEncoderUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  社工服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Service
public class SocialWorkerServiceImpl extends ServiceImpl<SocialWorkerMapper, SocialWorker> implements ISocialWorkerService {

    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(SocialWorkerLoginDTO socialWorkerLoginDTO) {
        String workerNo = socialWorkerLoginDTO.getWorkerNo();
        String password = socialWorkerLoginDTO.getPassword();
        
        // 查询社工信息
        LambdaQueryWrapper<SocialWorker> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SocialWorker::getWorkerNo, workerNo);
        SocialWorker socialWorker = this.getOne(queryWrapper);
        
        // 验证社工是否存在以及密码是否正确
        if (socialWorker == null || !passwordEncoderUtil.matches(password, socialWorker.getPassword())) {
            throw new BusinessException(CodeConstant.INVALID_CREDENTIALS_CODE, MessageConstant.PASSWORD_ERROR);
        }
        
        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, socialWorker.getId());
        claims.put(JwtClaimsConstant.USER_NAME, socialWorker.getName());
        claims.put(JwtClaimsConstant.USER_ROLE, 2); // 2表示社工角色
        String token = jwtUtil.createJWT(claims);
        
        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setId(socialWorker.getId());
        loginVO.setName(socialWorker.getName());
        loginVO.setToken(token);
        loginVO.setRole(2);
        
        return loginVO;
    }

    @Override
    public SocialWorkerInfoVO getSocialWorkerInfo(Long id) {
        SocialWorker socialWorker = this.getById(id);
        if (socialWorker == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"社工不存在");
        }
        
        SocialWorkerInfoVO socialWorkerInfoVO = new SocialWorkerInfoVO();
        BeanUtils.copyProperties(socialWorker, socialWorkerInfoVO);
        return socialWorkerInfoVO;
    }

    @Override
    public void updateSocialWorkerInfo(UpdateSocialWorkerInfoDTO updateSocialWorkerInfoDTO) {
        SocialWorker socialWorker = new SocialWorker();
        BeanUtils.copyProperties(updateSocialWorkerInfoDTO, socialWorker);
        
        boolean result = this.updateById(socialWorker);
        if (!result) {
            throw new BusinessException(CodeConstant.FAIL_CODE,"更新社工信息失败");
        }
    }

    @Override
    public void logout() {
        // 清除ThreadLocal中的用户信息
        BaseContext.clear();
    }
}