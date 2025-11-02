package com.caresync.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt") // 指定前缀，自动绑定yml中jwt开头的配置
public class JwtConfig {
    /**
     * JWT 密钥
     */
    private String secret;
    /**
     * JWT 过期时间（单位：秒）
     */
    private Long expiration;
    /**
     * JWT 请求头名称
     */
    private String header;
}
