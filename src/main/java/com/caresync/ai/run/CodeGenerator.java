package com.caresync.ai.run;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.config.builder.GeneratorBuilder;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;

import java.util.Collections;

/**
 * 代码生成器 - 适配care-sync-ai项目
 */
public class CodeGenerator {
    /**
     * 主方法，用于生成代码
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 数据库连接配置
        String url = "jdbc:postgresql://localhost:5433/care_sync_db?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true";
        String username = "postgres";
        String password = "1829002";

        // 生成代码的包路径 - 适配当前项目结构
        String parentPackage = "com.caresync.ai";
        String entityPackage = "com.caresync.ai.model.entity";

        // 输出目录 - 适配当前项目结构
        String projectPath = System.getProperty("user.dir");
        String mapperPath = projectPath + "/src/main/resources/mapper/";

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("Maou") // 设置作者
                            //.enableSwagger() // 开启Swagger注解（用于Knife4j文档生成）
                            .dateType(DateType.TIME_PACK) // 设置日期类型
                            .outputDir(projectPath + "/src/main/java"); // 设置输出目录
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(parentPackage) // 设置父包名
                            .entity(entityPackage) // 实体类包路径
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.Impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, mapperPath)); // 设置mapper.xml路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    // 设置需要生成的表名 - 适配care_sync_db数据库中的表
                    builder.addInclude("child", "ai_chat_record", "ai_assist_scheme", "assist_track_log", "ai_analysis_log", "social_worker")
                            // 实体类策略配置
                            // 添加文件覆盖配置
                            .entityBuilder()
                            .enableFileOverride() // 开启实体类文件覆盖
                            .enableLombok() // 开启Lombok
                            .enableRemoveIsPrefix() // 开启Boolean类型字段移除is前缀
                            .enableTableFieldAnnotation() // 启用字段注解（用于生成Swagger注解）
                            .enableActiveRecord() // 启用ActiveRecord模式
                            .addTableFills(new Column("create_time", FieldFill.INSERT)) // 自动填充创建时间
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE)) // 自动填充更新时间
                            // Mapper策略配置
                            .mapperBuilder()
                            .enableFileOverride() // 开启Mapper文件覆盖
                            .enableMapperAnnotation() // 开启@Mapper注解
                            .superClass(BaseMapper.class) // 设置BaseMapper父类
                            // Service策略配置
                            .serviceBuilder()
                            .enableFileOverride() // 开启Service文件覆盖
                            .superServiceClass(IService.class) // 设置IService父类
                            .superServiceImplClass(ServiceImpl.class) // 设置ServiceImpl父类
                            // Controller策略配置
                            .controllerBuilder()
                            .enableFileOverride() // 开启Controller文件覆盖
                            .enableRestStyle() // 开启Rest风格
                            .enableHyphenStyle() // 开启连字符命名风格
                            //.enableControllerAnnotation() // 开启Controller注解
                            // 自定义Controller类名后缀
                            .formatFileName("%sController");
                })
                // 使用Freemarker引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行生成
                .execute();
    }
}