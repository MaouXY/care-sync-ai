package com.caresync.ai.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 创建自定义业务异常基类
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    private String errorCode;
    private String errorMessage;

    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
