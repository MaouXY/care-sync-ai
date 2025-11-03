package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 儿童登录请求参数
 */
@Data
public class ChildLoginDTO {
    private String childNo; // 儿童登录ID
    private String verifyCode; // 4位登录验证码
}