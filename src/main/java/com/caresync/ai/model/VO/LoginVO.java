package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应结果
 */
@Data
public class LoginVO implements Serializable {
    private Long id; // 用户ID
    private String name; // 用户名
    private String token; // JWT令牌
    private Integer role; // 用户角色(1:儿童, 2:社工)
}