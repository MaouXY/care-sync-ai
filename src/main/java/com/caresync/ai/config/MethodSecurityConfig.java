package com.caresync.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig {
    // Spring Security 6.x 简化配置
    // 方法级安全控制已通过@EnableMethodSecurity注解启用
    // 不再需要继承GlobalMethodSecurityConfiguration
}