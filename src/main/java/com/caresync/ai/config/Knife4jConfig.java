package com.caresync.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Knife4j配置类
 * 用于配置OpenAPI文档生成
 */
@Configuration
public class Knife4jConfig {
    
    /**
     * 创建OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 设置文档信息
                .info(new Info()
                        // 文档标题
                        .title("CareSync AI API文档")
                        // 文档描述
                        .description("CareSync AI儿童关怀系统API接口文档")
                        // 版本信息
                        .version("1.0.0")
                        // 联系人信息
                        .contact(new Contact()
                                .name("Maou"))
                        // 许可证信息
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}