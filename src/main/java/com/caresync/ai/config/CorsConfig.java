package com.caresync.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS配置类
 * 用于处理跨域请求
 * ？？？
 * ？？？
 */
/*//TODO
* 问题分析 ：
* 通过比较前端代码和API测试工具的差异，发现了问题的根本原因：
* 当前端设置 withCredentials: true （允许携带凭证如cookies）时，
* 浏览器会强制执行更严格的CORS安全策略，要求后端不能使用通配符 * 作为allowedOriginPatterns。
* 这就解释了为什么ApiPost测试工具能正常工作，但浏览器中前端请求失败。
* */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置CORS映射
     * 允许所有接口的跨域请求
     * 指定允许的前端域名、请求方法、请求头、是否允许携带凭证
     * 设置预检请求的有效期为1小时
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有接口
                //添加跨域请求允许的前端地址
                .allowedOriginPatterns("http://localhost:90", "http://127.0.0.1:90","http://localhost:5173","http://localhost:5175","https://55a0ec7f.r40.cpolar.top") // 指定允许的前端域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许发送cookie
                .maxAge(3600); // 预检请求的有效期，单位秒
    }

    /**
     * 配置CORS全局配置
     * 用于处理跨域请求的全局配置
     * 允许所有接口的跨域请求
     * 指定允许的前端域名、请求方法、请求头、是否允许携带凭证
     * 设置预检请求的有效期为1小时
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //添加跨域请求允许的前端地址
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:90", "http://127.0.0.1:90","http://localhost:5173","http://localhost:5175","https://55a0ec7f.r40.cpolar.top"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}