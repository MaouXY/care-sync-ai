package com.caresync.ai;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.caresync.ai.mapper")
@ConfigurationPropertiesScan("com.caresync.ai") // 扫描所有子包
@EnableCaching// 开启缓存
@Slf4j
public class CareSyncAiApplication {

    public static void main(String[] args) {

        SpringApplication.run(CareSyncAiApplication.class, args);
        log.info("######### CareSync-AI Server Started! #########");
    }

}
