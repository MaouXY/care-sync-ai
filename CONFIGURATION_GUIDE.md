# CareSync AI 项目配置指南

## 敏感信息保护

为了保护您的敏感信息（如API密钥、数据库密码等），项目已配置了.gitignore文件来防止这些信息被提交到远程仓库。

## 配置步骤

### 1. 创建本地配置文件

复制模板文件并重命名为实际的配置文件：

```bash
# 复制模板文件
cp src/main/resources/application-template.yml src/main/resources/application.yml

# 复制环境变量示例文件
cp .env.example .env
```

### 2. 配置敏感信息

编辑 `src/main/resources/application.yml` 文件，修改以下敏感配置项：

```yaml
# 数据库密码
spring:
  datasource:
    password: your-actual-database-password

# 大模型API密钥
big:
  model:
    api:
      key: your-actual-big-model-api-key

# JWT密钥
jwt:
  secret: your-actual-jwt-secret

# 密码加密密钥
password:
  secret: your-actual-password-secret
```

### 3. 使用环境变量（推荐）

您也可以使用环境变量来配置敏感信息。编辑 `.env` 文件：

```bash
# 数据库配置
DB_PASSWORD=your-actual-database-password

# 大模型API配置
BIG_MODEL_API_KEY=your-actual-big-model-api-key

# JWT配置
JWT_SECRET=your-actual-jwt-secret

# 密码加密配置
PASSWORD_SECRET=your-actual-password-secret
```

然后在 `application.yml` 中使用环境变量引用：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}

big:
  model:
    api:
      key: ${BIG_MODEL_API_KEY}

jwt:
  secret: ${JWT_SECRET}

password:
  secret: ${PASSWORD_SECRET}
```

### 4. 阿里云OSS配置（可选）

如果您需要使用阿里云OSS功能，请取消注释并配置以下内容：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${ALIYUN_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
    region: cn-hangzhou
    bucketName: hcoj
```

并在 `.env` 文件中添加：

```bash
ALIYUN_ACCESS_KEY_ID=your-actual-aliyun-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-actual-aliyun-access-key-secret
```

## 安全注意事项

1. **永远不要提交敏感信息到版本控制**
   - `application.yml` 文件已被添加到 `.gitignore`
   - `.env` 文件已被添加到 `.gitignore`

2. **使用强密码和密钥**
   - 数据库密码应使用强密码
   - API密钥和JWT密钥应使用足够长度的随机字符串

3. **定期轮换密钥**
   - 定期更换API密钥和JWT密钥
   - 在生产环境中使用密钥管理服务

4. **开发和生产环境分离**
   - 为开发、测试和生产环境使用不同的配置
   - 生产环境配置应通过安全的方式管理

## 故障排除

### 配置不生效
- 确保 `application.yml` 文件在 `src/main/resources/` 目录下
- 检查文件名拼写是否正确
- 重启应用使配置生效

### 环境变量不生效
- 确保 `.env` 文件在项目根目录
- 检查环境变量名称是否与配置文件中的引用一致
- 某些IDE可能需要重启才能识别新的环境变量文件

## 支持

如有配置问题，请参考项目文档或联系开发团队。