package com.caresync.ai.controller;

import com.caresync.ai.model.DTO.ChangeSchemeStatusDTO;
import com.caresync.ai.model.DTO.GenerateSchemeDTO;
import com.caresync.ai.model.DTO.SchemeQueryDTO;
import com.caresync.ai.model.DTO.UpdateSchemeDTO;
import com.caresync.ai.model.VO.AssistSchemeListVO;
import com.caresync.ai.model.VO.AssistSchemeVO;
import com.caresync.ai.model.VO.DetailSchemeVO;
import com.caresync.ai.result.PageResult;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.IAiAssistSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 服务方案模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/social-worker/scheme")
@Tag(name = "服务方案模块接口", description = "AI服务方案相关接口")
public class AiAssistSchemeController {

    @Autowired
    private IAiAssistSchemeService aiAssistSchemeService;

    /**
     * AI生成服务方案
     * @param generateSchemeDTO 生成方案DTO
     * @return 服务方案VO
     */
    @PostMapping("/generate")
    @Operation(summary = "生成AI服务方案", description = "一键生成AI服务方案")
    public Result<AssistSchemeVO> generateScheme(@RequestBody GenerateSchemeDTO generateSchemeDTO) {
        try {
            AssistSchemeVO result = aiAssistSchemeService.generateScheme(generateSchemeDTO);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("生成服务方案失败", e);
            return Result.error("生成服务方案失败，请稍后重试");
        }
    }

    /**
     * 获取方案列表
     * @param schemeQueryDTO 查询参数DTO
     * @return 分页结果
     */
    @PostMapping("/list")
    @Operation(summary = "获取方案列表", description = "按儿童ID或状态查询服务方案列表")
    public Result<PageResult<AssistSchemeListVO>> getSchemeList(@RequestBody SchemeQueryDTO schemeQueryDTO) {
        PageResult<AssistSchemeListVO> result = aiAssistSchemeService.getSchemeList(schemeQueryDTO);
        return Result.success(result);
    }

    /**
     * 获取方案详情
     * @param id 方案ID
     * @return 服务方案详情VO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取方案详情", description = "获取指定方案的详细信息")
    public Result<DetailSchemeVO> getSchemeDetail(@PathVariable Long id) {
        DetailSchemeVO detailSchemeVO = aiAssistSchemeService.getSchemeDetail(id);
        if (detailSchemeVO == null) {
            return Result.error("方案不存在");
        }
        return Result.success(detailSchemeVO);
    }

    /**
     * 编辑方案
     * @param id 方案ID
     * @param updateSchemeDTO 更新方案DTO
     * @return 服务方案VO
     */
    //@PutMapping("/update/{id}")
    //@Operation(summary = "编辑方案", description = "AI交互式修改服务方案")
    public Result<AssistSchemeVO> updateScheme(@PathVariable Long id, @RequestBody UpdateSchemeDTO updateSchemeDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 修改方案状态
     * @param id 方案ID
     * @param changeSchemeStatusDTO 状态变更DTO
     * @return 服务方案VO
     */
    //@PutMapping("/status/{id}")
    //@Operation(summary = "修改方案状态", description = "更改服务方案的状态")
    public Result<AssistSchemeVO> changeSchemeStatus(@PathVariable Long id, @RequestBody ChangeSchemeStatusDTO changeSchemeStatusDTO) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 获取所有方案状态
     * @return 方案状态列表
     */
    //@GetMapping("/status-options")
    //@Operation(summary = "获取方案状态选项", description = "获取所有可用的服务方案状态选项")
    public Result<String[]> getSchemeStatusOptions() {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 管理视角获取方案列表
     * @param schemeQueryDTO 查询参数DTO
     * @return 分页结果
     */
    //@PostMapping("/manage/list")
    //@Operation(summary = "管理视角获取方案列表", description = "管理视角按条件查询所有服务方案列表")
    public Result<PageResult<AssistSchemeListVO>> getSchemeListManage(@RequestBody SchemeQueryDTO schemeQueryDTO) {
        PageResult<AssistSchemeListVO> result = aiAssistSchemeService.getSchemeListManage(schemeQueryDTO);
        return Result.success(result);
    }

    /**
     * 管理视角获取方案详情
     * @param id 方案ID
     * @return 服务方案详情VO
     */
    //@GetMapping("/manage/detail/{id}")
    //@Operation(summary = "管理视角获取方案详情", description = "管理视角获取指定方案的详细信息")
    public Result<DetailSchemeVO> getSchemeDetailManage(@PathVariable Long id) {
        DetailSchemeVO detailSchemeVO = aiAssistSchemeService.getSchemeDetail(id);
        if (detailSchemeVO == null) {
            return Result.error("方案不存在");
        }
        return Result.success(detailSchemeVO);
    }
}