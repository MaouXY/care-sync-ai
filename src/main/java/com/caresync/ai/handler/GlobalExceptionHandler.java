package com.caresync.ai.handler;

import com.caresync.ai.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * 全局异常处理器，统一捕获和处理应用中的异常
 */

@Hidden // 隐藏全局异常处理器，不展示在API文档中
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理通用异常（兜底处理未定义的异常）
     * @param ex 异常对象
     * @param request 请求对象
     * @return 统一的错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleGlobalException(Exception ex, WebRequest request) {
        // 记录错误日志
        log.error("系统异常：", ex);

        // 构建错误响应
        Result result = Result.error("系统内部错误，请联系管理员");

        // 返回500状态码和错误信息
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(result);
    }

    /**
     * 处理参数校验异常
     * @param ex 参数校验异常对象
     * @param request 请求对象
     * @return 统一的错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // 记录参数异常日志
        log.warn("参数异常：{}", ex.getMessage());

        // 构建错误响应
        Result result = Result.error(ex.getMessage());

        // 返回400状态码和错误信息
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(result);
    }
}
