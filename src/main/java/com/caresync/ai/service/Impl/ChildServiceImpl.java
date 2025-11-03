package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.CodeConstant;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.constant.MessageConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.exception.BusinessException;
import com.caresync.ai.model.DTO.ChildLoginDTO;
import com.caresync.ai.model.DTO.UpdateChildInfoDTO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.ChildMapper;
import com.caresync.ai.service.IChildService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  儿童服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Service
public class ChildServiceImpl extends ServiceImpl<ChildMapper, Child> implements IChildService {
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public LoginVO login(ChildLoginDTO childLoginDTO) {
        String childNo = childLoginDTO.getChildNo();
        String verifyCode = childLoginDTO.getVerifyCode();
        
        // 查询儿童信息
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Child::getChildNo, childNo);
        Child child = this.getOne(queryWrapper);
        
        // 验证儿童是否存在以及验证码是否正确
        if (child == null || !verifyCode.equals(child.getVerifyCode())) {
            throw new BusinessException(CodeConstant.INVALID_CREDENTIALS_CODE,MessageConstant.PASSWORD_ERROR);
        }
        
        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, child.getId());
        claims.put(JwtClaimsConstant.USER_NAME, child.getName());
        claims.put(JwtClaimsConstant.USER_ROLE, 1); // 1表示儿童角色
        String token = JwtUtil.createJWT(jwtConfig.getSecret(), jwtConfig.getExpiration() * 1000, claims);
        
        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setId(child.getId());
        loginVO.setName(child.getName());
        loginVO.setToken(token);
        loginVO.setRole(1);
        
        return loginVO;
    }

    @Override
    public ChildInfoVO getChildInfo(Long id) {
        Child child = this.getById(id);
        if (child == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"儿童不存在");
        }
        
        ChildInfoVO childInfoVO = new ChildInfoVO();
        BeanUtils.copyProperties(child, childInfoVO);
        return childInfoVO;
    }

    @Override
    public void updateChildInfo(UpdateChildInfoDTO updateChildInfoDTO) {
        Child child = new Child();
        BeanUtils.copyProperties(updateChildInfoDTO, child);
        
        boolean result = this.updateById(child);
        if (!result) {
            throw new BusinessException(CodeConstant.FAIL_CODE,"更新儿童信息失败");
        }
    }

    @Override
    public void logout() {
        // 清除ThreadLocal中的用户信息
        BaseContext.clear();
    }
}