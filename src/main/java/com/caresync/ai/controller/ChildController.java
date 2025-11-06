package com.caresync.ai.controller;

import com.caresync.ai.context.BaseContext;
import com.caresync.ai.livetalking.LiveTalkingService;
import com.caresync.ai.model.DTO.*;
import com.caresync.ai.model.VO.*;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IAiChatRecordService;
import com.caresync.ai.service.IChildService;
import com.caresync.ai.service.Impl.AiChatRecordServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 儿童模块控制器
 */
@RestController
@RequestMapping("/api/child")
@Tag(name = "儿童模块接口", description = "儿童用户相关接口")
public class ChildController {

    @Autowired
    private IChildService childService;

    @Autowired
    private LiveTalkingService liveTalkingService;

    @Autowired
    private IAiChatRecordService AiChatRecordService;

    /**
     * 儿童登录
     * @param childLoginDTO 儿童登录DTO
     * @return 登录VO
     */
    @PostMapping("/login")
    @Operation(summary = "儿童登录", description = "儿童ID+4位验证码登录")
    public Result<LoginVO> login(@RequestBody ChildLoginDTO childLoginDTO) {
        LoginVO loginVO = childService.login(childLoginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取儿童个人信息
     * @return 儿童信息VO
     */
    @GetMapping("/info")
    @Operation(summary = "获取儿童个人信息", description = "获取儿童基本信息")
    public Result<ChildVO> getChildInfo() {
        // 从上下文中获取当前登录儿童的ID
        Long childId = BaseContext.getCurrentId();
        // 调用服务层获取儿童信息
        ChildVO childVO = childService.getChild(childId);
        return Result.success(childVO);
    }

    /**
     * 发送聊天消息
     * @param chatMessageDTO 聊天消息DTO
     * @return 结果
     */
    @PostMapping("/chat/send")
    @Operation(summary = "发送聊天消息", description = "文字/语音输入")
    public Result<ChildChatMessageVO> sendChatMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        try {
            ChildChatMessageVO childChatMessageVO = liveTalkingService.sendChatMessage(chatMessageDTO);
            return Result.success(childChatMessageVO);
        } catch (Exception e) {
            return Result.error("发送聊天消息失败");
        }
    }

    /**
     * 获取聊天记录
     * @param sessionId 会话ID
     * @return 聊天消息列表
     */
    @GetMapping("/chat/history")
    @Operation(summary = "获取聊天记录", description = "获取历史聊天记录")
    public Result<List<com.caresync.ai.model.VO.ChatMessageVO>> getChatHistory(@RequestParam String sessionId) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 紧急呼叫
     * @param emergencyCallDTO 紧急呼叫DTO
     * @return 结果
     */
    @PostMapping("/emergency-call")
    @Operation(summary = "紧急呼叫", description = "触发紧急联系")
    public Result emergencyCall(@RequestBody EmergencyCallDTO emergencyCallDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    // 以下是儿童管理模块接口（社工端使用）

    /**
     * 获取儿童列表
     * @param childQueryDTO 儿童查询DTO
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "获取儿童列表", description = "分页查询，显示ID、姓名、年龄，标注新聊天记录")
    public Result<PageResult<ChildInfoVO>> getChildList(ChildQueryDTO childQueryDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 获取儿童详情
     * @param id 儿童ID
     * @return 儿童详情VO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取儿童详情", description = "显示AI提取的结构化信息")
    public Result<ChildInfoVO> getChildDetail(@PathVariable Long id) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success(null);
    }

    /**
     * 创建儿童账号
     * @param createChildDTO 创建儿童DTO
     * @return 结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建儿童账号", description = "社工预先创建儿童账号")
    public Result createChild(@RequestBody CreateChildDTO createChildDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 更新儿童信息
     * @param id 儿童ID
     * @param updateChildInfoDTO 更新儿童信息DTO
     * @return 结果
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "更新儿童信息", description = "更新儿童基本信息")
    public Result updateChildInfo(@PathVariable Long id, @RequestBody UpdateChildInfoDTO updateChildInfoDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 重置儿童验证码
     * @param id 儿童ID
     * @return 结果
     */
    @PostMapping("/reset-code/{id}")
    @Operation(summary = "重置儿童验证码", description = "重置儿童的登录验证码")
    public Result resetChildVerifyCode(@PathVariable Long id) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }
}