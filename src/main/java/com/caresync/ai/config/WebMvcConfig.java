package com.caresync.ai.config;

import com.caresync.ai.interceptor.AdminOnlyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminOnlyInterceptor adminOnlyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册管理员权限拦截器
        registry.addInterceptor(adminOnlyInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns( //TODO 需要重新配置
                        "/user/register", "/api/social-worker/login", "/api/social-worker/logout","/api/child/login",
                        "/error", "/common/upload", "/api/judge0/webhook/callback",
                        "/static/**", "/resources/**"
                );
    }
}
