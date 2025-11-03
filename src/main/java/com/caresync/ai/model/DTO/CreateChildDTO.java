package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 创建儿童账号请求参数
 */
@Data
public class CreateChildDTO {
    private String childNo; // 儿童登录ID
    private String name; // 儿童姓名
    private Integer age; // 儿童年龄
    private String verifyCode; // 4位登录验证码
}