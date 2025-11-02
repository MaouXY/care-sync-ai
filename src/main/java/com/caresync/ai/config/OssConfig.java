package com.caresync.ai.config;

import com.caresync.ai.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建AliOSSUtil
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Slf4j
public class OssConfig {
    /**
     * OSS 服务端地址
     */
    private String endpoint;
    /**
     * 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
     */
    private String accessKeyId;
    /**
     * 阿里云账号AccessKeySecret是用于加密签名字符串和服务器端验证签名字符串的密钥，建议您妥善保管。
     */
    private String accessKeySecret;
    /**
     * OSS 区域
     */
    private String region;
    /**
     * OSS 存储桶名称
     */
    private String bucketName;

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil() {
        log.info("开始创建阿里云文件上传工具类：{}", this);
        return new AliOssUtil(endpoint,
                accessKeyId,
                accessKeySecret,
                bucketName);
    }
}
