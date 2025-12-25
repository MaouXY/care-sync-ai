# CareSync AI 留守儿童情感陪伴与服务项目

<div align="center">

![CareSync AI Logo](https://img.shields.io/badge/CareSync-AI-blue?style=for-the-badge)

专注于留守儿童情感陪伴与服务的创新项目，构建"AI智能陪伴+社工精准介入"的轻量化服务体系

[![SpringBoot](https://img.shields.io/badge/SpringBoot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.x-blue.svg)](https://www.postgresql.org/)
[![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

## 📋 项目概述

CareSync AI 是一个专注于留守儿童情感陪伴与服务的创新项目，通过安全合规的AI交互填补日常情感空白，依托结构化数据支撑社工开展针对性服务。

### 🎯 核心价值

- **儿童端**：提供有温度、可信赖的即时情感回应和安全的倾诉出口
- **社工端**：通过AI提炼的结构化数据快速定位儿童需求，实现数据驱动的精准服务
- **管理端**：形成交互-分析-服务-跟踪的闭环数据沉淀，提供可量化的效果评估依据

## 🏗️ 技术架构

### 核心技术栈

| 技术层面 | 选型方案 | 优势特点 |
|---------|---------|---------|
| 后端框架 | SpringBoot 3.x | 轻量化开发，支持快速集成第三方服务 |
| 数据库 | PostgreSQL 15.x | 原生支持JSONB类型与复杂查询 |
| 前端技术 | Vue 3 + Element Plus | 组件化开发效率高，UI组件适配管理系统 |
| AI模型 | 定制化儿童专用大模型 | 优化儿童语言理解能力，内置安全规则 |
| 语音处理 | 阿里云ASR/TTS | 识别准确率高，支持儿童语音优化 |
| 数字人交互 | livetalking服务 | 专注数字人实例管理与画面传输 |

### 系统架构设计

项目采用分层架构设计，包括：
- **前端展示层**：基于Vue 3的用户界面
- **后端服务层**：SpringBoot RESTful API服务
- **数据存储层**：PostgreSQL数据库与Redis缓存
- **AI能力层**：定制化AI模型与第三方服务集成

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 15.x
- Redis 6.x+
- Node.js 16+ (前端开发)

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/care-sync-ai.git
   cd care-sync-ai
   ```

2. **配置数据库**
   ```bash
   # 创建数据库
   createdb careshync_ai
   
   # 执行初始化脚本
   psql -d careshync_ai -f src/main/resources/sql/database_init.sql
   ```

3. **配置应用**
   ```bash
   # 复制配置文件模板
   cp src/main/resources/application-template.yml src/main/resources/application.yml
   
   # 修改配置文件中的数据库连接等信息
   ```

4. **启动后端服务**
   ```bash
   # 使用Maven启动
   mvn spring-boot:run
   
   # 或者打包后启动
   mvn clean package
   java -jar target/care-sync-ai-0.0.1-SNAPSHOT.jar
   ```

5. **启动前端服务** (可选)
   ```bash
   cd src/main/resources/static/livetalking-vue
   npm install
   npm run serve
   ```

6. **访问应用**
   - 后端API文档：http://localhost:8080/doc.html
   - 前端界面：http://localhost:8080 (如果已部署前端)

## 📚 项目结构

```
care-sync-ai/
├── src/
│   ├── main/
│   │   ├── java/com/caresync/ai/          # 后端Java代码
│   │   │   ├── controller/                # 控制器层
│   │   │   ├── service/                   # 服务层
│   │   │   ├── mapper/                    # 数据访问层
│   │   │   ├── model/                     # 数据模型
│   │   │   ├── config/                    # 配置类
│   │   │   ├── utils/                     # 工具类
│   │   │   └── ...                        # 其他模块
│   │   └── resources/
│   │       ├── static/                    # 静态资源
│   │       ├── templates/                 # 模板文件
│   │       ├── md/                        # 项目文档
│   │       ├── sql/                       # 数据库脚本
│   │       └── application.yml            # 应用配置
│   └── test/                              # 测试代码
├── pom.xml                                # Maven配置
└── README.md                              # 项目说明
```

## 🔧 主要功能

### 儿童端：安全型AI情感陪伴系统
- **极简登录**：采用"儿童ID+4位验证码"组合登录，保障账号安全
- **安全交互**：支持语音+文本双输入，数字人画面输出，全流程安全防护
- **核心功能**：即时聊天、故事点播、紧急呼叫

### 社工端：数据驱动的服务管理系统
- **儿童管理**：支持批量创建和个性化信息录入
- **AI分析查看**：展示情感趋势、核心需求、潜在问题等结构化信息
- **服务方案生成**：AI一键生成服务方案，支持编辑调整
- **进度跟踪**：记录服务执行情况，形成完整服务档案

### 系统后台：自动化数据处理
- **AI分析引擎**：定时+触发双模式分析聊天记录
- **内容安全过滤**：双重过滤机制保障交互安全
- **数据存储优化**：结构化与非结构化数据结合存储

## 🌟 创新点与技术优势

### 模式创新：AI+社工协同服务
创新性地提出"AI智能陪伴+社工精准介入"的服务模式，通过AI技术弥补社工资源不足，实现技术与人文关怀的有机结合。

### 技术创新：定制化儿童AI模型
开发定制化的儿童专用大模型，针对儿童语言特点和心理需求进行深度优化，解决通用AI模型在儿童服务场景中的适应性问题。

### 数据创新：结构化数据支撑精准服务
通过AI技术将非结构化的会话内容转化为结构化的分析结果，为社工提供清晰、直观的数据支持，提高服务的精准性和效率。

## 📊 项目成功指标

### 儿童端指标
- 日均活跃率≥40%（每周至少使用3天）
- 单次交互时长≥3分钟
- 安全过滤触发率≤5%
- 紧急呼叫功能月使用率≥2%

### 社工端指标
- 帮扶方案生成周期≤1天
- 方案完成率≥80%
- 社工对AI分析结果的满意度≥85%

### 系统指标
- 服务可用性≥99.9%
- 核心接口响应时间≤500ms
- 支持同时在线用户≥500人

## 🤝 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 🐛 报告Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复

### 提交规范

- 提交信息需清晰描述更改内容，使用中文
- 格式：`[类型] 描述`，例如 `[修复] 解决登录验证码错误问题`
- 类型包括：功能、修复、文档、样式、重构、测试、构建

### 开发流程

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m '[功能] 添加某个功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 项目主页：[https://github.com/your-username/care-sync-ai](https://github.com/your-username/care-sync-ai)
- 问题反馈：[Issues](https://github.com/your-username/care-sync-ai/issues)
- 邮箱：caresync@example.com

## 🙏 致谢

感谢所有为 CareSync AI 项目做出贡献的开发者、设计师和测试人员。

---

<div align="center">

**[⬆ 回到顶部](#caresync-ai-留守儿童情感陪伴与服务项目)**

Made with ❤️ by CareSync AI Team

</div>