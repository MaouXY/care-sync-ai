package com.caresync.ai.controller;

import com.caresync.ai.context.BaseContext;
import com.caresync.ai.model.DTO.*;
import com.caresync.ai.model.VO.*;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.ISocialWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 社工模块控制器
 */
@RestController
@RequestMapping("/api/social-worker")
@Tag(name = "社工模块接口", description = "社工相关接口")
public class SocialWorkerController {

    @Autowired
    private ISocialWorkerService socialWorkerService;

    /**
     * 社工首页
     */
    @GetMapping("/home")
    @Operation(summary = "社工首页", description = "返回社工首页信息")
    public Result<SocialWorkerHomeVO> getSocialWorkerHome() {
        // 从ThreadLocal中获取当前登录的社工ID
        Long workerId = BaseContext.getCurrentId();
        // 调用service层方法获取社工首页信息
        SocialWorkerHomeVO socialWorkerHomeVO = socialWorkerService.getSocialWorkerHome(workerId);
        return Result.success(socialWorkerHomeVO);
    }

    /**
     * 社工登录
     * @param socialWorkerLoginDTO 社工登录DTO
     * @return 登录VO
     */
    @PostMapping("/login")
    @Operation(summary = "社工登录", description = "根据社工账号和密码登录")
    public Result<LoginVO> login(@RequestBody SocialWorkerLoginDTO socialWorkerLoginDTO) {
        // 保留原有实现，因为已经实现了登录功能
        return Result.success(socialWorkerService.login(socialWorkerLoginDTO));
    }

    /**
     * 社工登出
     * @return 结果
     */
    @PostMapping("/logout")
    @Operation(summary = "社工登出", description = "退出登录")
    public Result logout() {
        return Result.success("登出成功",null);
    }

    /**
     * 获取社工个人信息
     * @return 社工信息VO
     */
    @GetMapping("/info")
    @Operation(summary = "获取个人信息", description = "获取社工个人信息")
    public Result<SocialWorkerInfoVO> getSocialWorkerInfo() {
        // 从ThreadLocal中获取当前登录的社工ID
        Long workerId = BaseContext.getCurrentId();
        // 调用service层方法获取社工个人信息
        SocialWorkerInfoVO socialWorkerInfoVO = socialWorkerService.getSocialWorkerInfo(workerId);
        return Result.success(socialWorkerInfoVO);
    }

    /**
     * 更新社工个人信息
     * @param updateSocialWorkerInfoDTO 更新社工信息DTO
     * @return 结果
     */
    @PutMapping("/info")
    @Operation(summary = "更新个人信息", description = "更新社工个人信息")
    public Result updateSocialWorkerInfo(@RequestBody UpdateSocialWorkerInfoDTO updateSocialWorkerInfoDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    // 儿童管理模块功能已移至ChildManageController控制器中

    // AI聊天模块

    /** TODO 取消该接口
     * 获取会话列表
     * @param childId 可选，儿童ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/chat/sessions")
    @Operation(summary = "获取会话列表", description = "获取儿童与AI的会话列表")
    public Result<PageResult<SessionVO>> getSessionList(@RequestParam(required = false) Long childId, 
                                                       @RequestParam(defaultValue = "1") Integer page, 
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 发送消息
     * @param messageDTO 消息DTO
     * @return 消息VO
     */
    @PostMapping("/chat/message")
    @Operation(summary = "发送消息", description = "社工向儿童发送消息")
    public Result<ChatMessageVO> sendMessage(@RequestBody ChatMessageDTO messageDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 获取聊天记录
     * @param sessionId 会话ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/chat/records")
    @Operation(summary = "获取聊天记录", description = "获取指定会话的聊天记录")
    public Result<PageResult<ChatMessageVO>> getChatRecords(@RequestParam String sessionId, 
                                                          @RequestParam(defaultValue = "1") Integer page, 
                                                          @RequestParam(defaultValue = "50") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    // 紧急呼叫模块

    /**
     * 获取紧急呼叫记录
     * @param childId 可选，儿童ID
     * @param page 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/emergency/calls")
    @Operation(summary = "获取紧急呼叫记录", description = "获取儿童的紧急呼叫记录")
    public Result<PageResult<EmergencyCallVO>> getEmergencyCalls(@RequestParam(required = false) Long childId, 
                                                              @RequestParam(defaultValue = "1") Integer page, 
                                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }
}