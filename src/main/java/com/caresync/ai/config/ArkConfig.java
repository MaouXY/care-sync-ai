package com.caresync.ai.config;

import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ark 模型配置类
 */
@Configuration
public class ArkConfig {

    @Value("${big.model.api.key}")
    private String apiKey;

    @Value("${big.model.api.url}")
    private String baseUrl;

    @Bean
    public ArkService arkService() {
        // 根据文档要求，每个进程仅初始化一次ArkService，作为单例使用
        return ArkService.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                //.timeout(Duration.ofSeconds(120))
                //.connectTimeout(Duration.ofSeconds(20))
                .retryTimes(2)
                .build();
    }

    // 当需要使用Access Key鉴权时，可以使用下面的配置（取消注释）
    /*
    @Value("${volc.accesskey}")
    private String ak;

    @Value("${volc.secretkey}")
    private String sk;

    @Bean
    public ArkService arkServiceWithAccessKey() {
        return ArkService.builder()
                .ak(ak)
                .sk(sk)
                .baseUrl(baseUrl)
                .build();
    }
    */
}
