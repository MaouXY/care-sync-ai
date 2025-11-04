package com.caresync.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import java.util.List;

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
        // 定义JWT安全方案
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);
        
        // 添加安全需求
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
        
        return new OpenAPI()
                // 设置服务器信息
                .servers(List.of(new Server().url("http://localhost:8080").description("开发环境")))
                // 设置文档信息
                .info(new Info()
                        // 文档标题
                        .title("CareSync AI API文档")
                        // 文档描述
                        .description("CareSync AI儿童关怀系统 - AI智能陪伴与社工精准介入的轻量化服务体系\n" +
                                "通过安全合规的AI交互填补日常情感空白，依托结构化数据支撑社工开展针对性服务")
                        // 版本信息
                        .version("1.0.0")
                        // 联系人信息
                        .contact(new Contact()
                                .name("Maou")
                                .email("maou@example.com")
                                .url("https://example.com"))
                        // 许可证信息
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // 添加组件（包含安全方案）
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                // 添加安全需求
                .addSecurityItem(securityRequirement);
    }
}