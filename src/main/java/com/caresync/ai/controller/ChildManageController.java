package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.ChildQueryDTO;
import com.caresync.ai.model.DTO.CreateChildDTO;
import com.caresync.ai.model.DTO.UpdateChildInfoDTO;
import com.caresync.ai.model.VO.ChildInfoVO;
import com.caresync.ai.model.VO.ChildQueueVO;
import com.caresync.ai.model.VO.ChildVO;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 儿童管理模块控制器
 */
@RestController
@RequestMapping("/api/social-worker/children")
@Tag(name = "儿童管理模块接口", description = "社工管理儿童相关接口")
public class ChildManageController {

    @Autowired
    private IChildService childService;

    /**
     * 获取儿童列表
     * @param childQueryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/list")
    @Operation(summary = "获取儿童列表", description = "分页查询儿童列表")
    public Result<PageResult<ChildQueueVO>> getChildList(@RequestBody ChildQueryDTO childQueryDTO) {
        // 调用service层方法获取儿童列表
        PageResult<ChildQueueVO> result = childService.getChildList(childQueryDTO);
        return Result.success(result);
    }

    /**
     * 获取儿童详情
     * @param id 儿童ID
     * @return 儿童信息VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取儿童详情", description = "根据ID获取儿童详细信息")
    public Result<ChildInfoVO> getChildDetail(@PathVariable Long id) {
        // 调用service层方法获取儿童详情
        ChildInfoVO childInfoVO = childService.getChildInfo(id);
        return Result.success(childInfoVO);
    }

    /**
     * 更新儿童信息
     * @param id 儿童ID
     * @param updateChildInfoDTO 更新儿童信息DTO
     * @return 结果
     */
    //@PutMapping("/{id}")
    //@Operation(summary = "更新儿童信息", description = "更新儿童信息")
    public Result updateChildInfo(@PathVariable Long id, @RequestBody UpdateChildInfoDTO updateChildInfoDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 重置儿童验证码
     * @param childNo 儿童账号
     * @return 结果
     */
    //@PostMapping("/{childNo}/reset-code")
    //@Operation(summary = "重置儿童验证码", description = "重置儿童登录验证码")
    public Result resetChildVerifyCode(@PathVariable String childNo) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 创建儿童账号
     * @param createChildDTO 创建儿童账号DTO
     * @return 结果
     */
    //@PostMapping
    //@Operation(summary = "创建儿童账号", description = "创建新的儿童账号")
    public Result createChild(@RequestBody CreateChildDTO createChildDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
}