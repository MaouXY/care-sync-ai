package com.caresync.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.CodeConstant;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.constant.MessageConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.exception.BusinessException;
import com.caresync.ai.model.DTO.ChildLoginDTO;
import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.UpdateChildInfoDTO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.ChildVO;
import com.caresync.ai.model.VO.LoginVO;
import com.caresync.ai.model.entity.Child;
import com.caresync.ai.mapper.ChildMapper;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.service.IChildService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caresync.ai.utils.JwtUtil;
import com.caresync.ai.utils.PasswordEncoderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  儿童服务实现类
 * </p>
 *
 * @author Maou
 * @since 2025-11-02
 */
@Slf4j
@Service
public class ChildServiceImpl extends ServiceImpl<ChildMapper, Child> implements IChildService {

    @Autowired
    private JwtConfig jwtConfig;

    /*####################儿童端####################*/
    /**
     * 儿童登录
     * @param childLoginDTO 儿童登录参数
     * @return 登录响应结果
     */
    @Override
    public LoginVO login(ChildLoginDTO childLoginDTO) {
        String childNo = childLoginDTO.getChildNo();
        String verifyCode = PasswordEncoderUtil.encode(childLoginDTO.getVerifyCode());

        // 查询儿童信息
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Child::getChildNo, childNo);
        Child child = this.getOne(queryWrapper);
        log.info("输入密码: {}", verifyCode);
        log.info("数据库密码: {}", child.getVerifyCode());

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

    /**
     * 获取儿童信息-儿童端
     * @param id 儿童ID
     * @return 儿童信息VO
     */
    @Override
    public ChildVO getChild(Long id) {
        Child child = this.getById(id);
        if (child == null) {
            throw new BusinessException(CodeConstant.NOT_FOUND_CODE,"儿童不存在");
        }

        ChildVO childVO = new ChildVO();
        BeanUtils.copyProperties(child, childVO);
        return childVO;
    }

    /**
     * 获取儿童信息
     * @param id 儿童ID
     * @return 儿童信息VO
     */
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

    /**
     * 更新儿童信息
     * @param updateChildInfoDTO 更新儿童信息参数
     */
    @Override
    public void updateChildInfo(UpdateChildInfoDTO updateChildInfoDTO) {
        Child child = new Child();
        BeanUtils.copyProperties(updateChildInfoDTO, child);

        boolean result = this.updateById(child);
        if (!result) {
            throw new BusinessException(CodeConstant.FAIL_CODE,"更新儿童信息失败");
        }
    }

    /**
     * 儿童注销登录
     */
    @Override
    public void logout() {
        // 清除ThreadLocal中的用户信息
        BaseContext.clear();
    }

    /*####################社工端####################*/
    /**
     * 获取儿童列表
     * @param childQueryDTO 儿童查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<ChildQueueVO> getChildList(ChildQueryDTO childQueryDTO) {
        // 构建分页对象
        Page<Child> page = new Page<>(childQueryDTO.getPage(), childQueryDTO.getPageSize());

        // 调用方法构建查询条件
        LambdaQueryWrapper<Child> queryWrapper = buildChildQueryWrapper(childQueryDTO);

        // 执行分页查询
        Page<Child> childPage = this.page(page, queryWrapper);

        // 转换为ChildQueueVO列表
        List<ChildQueueVO> childQueueVOList = childPage.getRecords().stream().map(child -> {
            ChildQueueVO childQueueVO = new ChildQueueVO();
            BeanUtils.copyProperties(child, childQueueVO);
            return childQueueVO;
        }).collect(java.util.stream.Collectors.toList());

        // 构建并返回分页结果
        return new PageResult<>(childPage.getTotal(), childQueueVOList);
    }

    /**
     * 构建儿童查询条件
     * @param childQueryDTO 查询条件DTO
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<Child> buildChildQueryWrapper(ChildQueryDTO childQueryDTO) {
        LambdaQueryWrapper<Child> queryWrapper = new LambdaQueryWrapper<>();

        // 儿童编号模糊查询
        if (childQueryDTO.getChildNo() != null) {
            queryWrapper.like(Child::getChildNo, childQueryDTO.getChildNo());
        }

        // 儿童姓名模糊查询
        if (childQueryDTO.getName() != null) {
            queryWrapper.like(Child::getName, childQueryDTO.getName());
        }

        // 最小年龄查询
        if (childQueryDTO.getMinAge() != null) {
            queryWrapper.ge(Child::getAge, childQueryDTO.getMinAge());
        }

        // 最大年龄查询
        if (childQueryDTO.getMaxAge() != null) {
            queryWrapper.le(Child::getAge, childQueryDTO.getMaxAge());
        }

        // 是否有新聊天记录查询
        if (childQueryDTO.getHasNewChat() != null) {
            queryWrapper.eq(Child::getHasNewChat, childQueryDTO.getHasNewChat());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Child::getCreateTime);

        return queryWrapper;
    }
}