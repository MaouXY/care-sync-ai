# 数据库设置指南

本指南将帮助你设置和配置CareSync AI项目所需的PostgreSQL数据库。

## 前提条件

在开始之前，请确保你已经安装了：
- PostgreSQL 10或更高版本
- psql命令行工具（PostgreSQL自带）

## 步骤1：创建数据库

使用以下命令连接到PostgreSQL并创建数据库：

```bash
# 连接到PostgreSQL服务器
psql -U postgres

# 在psql提示符下执行以下命令创建数据库
CREATE DATABASE care_sync_db;

# 退出psql
\q
```

或者，你可以直接使用提供的SQL脚本初始化整个数据库：

```bash
psql -U postgres -f database_init_fixed.sql
```

**注意：** 如果遇到"语法错误 在'-'或附近的"错误，请使用修复后的`database_init_fixed.sql`文件，该文件已解决格式和编码问题。

## 步骤2：配置应用程序连接

项目的数据库连接配置位于`src/main/resources/application.yml`文件中。请确保配置与你的PostgreSQL安装匹配：

```yaml
spring:
  # 数据源配置
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/care_sync_db?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: postgres
    password: 1829002  # 根据你的PostgreSQL设置修改密码

  # MyBatis Plus配置
  mybatis-plus:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.caresync.ai.model.entity
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## 步骤3：执行数据库初始化脚本

如果你需要自定义数据库表结构，可以修改`database_init_fixed.sql`文件，然后执行：

```bash
psql -U postgres -d care_sync_db -f database_init_fixed.sql
```

**注意：** 如果遇到"语法错误 在'-'或附近的"错误，请确保使用修复后的`database_init_fixed.sql`文件。

## 数据库结构说明

初始化脚本创建了以下表：

1. **child** - 存储儿童基本信息
2. **ai_chat_record** - 存储儿童与AI的聊天记录
3. **ai_assist_scheme** - 存储AI生成的帮扶方案
4. **assist_track_log** - 存储帮扶进度日志
5. **ai_analysis_log** - 存储AI分析记录
6. **social_worker** - 存储社工信息

## 代码生成器使用说明

项目包含一个代码生成器工具，可以自动生成实体类、Mapper、Service和Controller等代码，并自动添加Knife4j注解用于API文档生成。

### 使用方法

1. 确保数据库已经创建并包含上述表结构
2. 运行`com.caresync.ai.run.CodeGenerator`类的`main`方法
3. 代码将自动生成到正确的包路径中

### Knife4j文档访问

项目已集成Knife4j API文档工具，启动项目后可以通过以下地址访问：
- Knife4j文档首页：http://localhost:8080/api/doc.html
- OpenAPI 3.0规范：http://localhost:8080/api/v3/api-docs

### 生成的代码结构

```
com.caresync.ai
├── model/entity/       # 实体类
├── mapper/             # Mapper接口
├── service/            # Service接口
├── service/impl/       # Service实现
└── controller/         # 控制器
```

## 注意事项

1. 在生产环境中，请修改默认密码以提高安全性
2. 确保PostgreSQL服务正在运行
3. 如果你更改了数据库连接信息，请同步更新`application.yml`文件和`CodeGenerator.java`中的数据库连接参数

## 常见问题

**Q: 连接数据库时出现认证错误？**
A: 请检查PostgreSQL的pg_hba.conf配置文件，确保允许密码认证。

**Q: 创建数据库时权限不足？**
A: 确保你使用的PostgreSQL用户有创建数据库的权限。

**Q: 表已经存在的错误？**
A: 如果你已经创建过表，可以先删除旧表，或者跳过脚本中的CREATE DATABASE部分，只执行表创建部分。