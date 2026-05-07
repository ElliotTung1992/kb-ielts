# kb-ielts

独立的雅思学习 Spring Boot 应用，提供内容管理、每日学习计划、间隔复习、学习统计和静态前端页面。

## 技术栈

- Java 21
- Spring Boot 3.4.1
- MyBatis + PageHelper
- PostgreSQL 16
- Liquibase
- Maven
- 静态 HTML + Bootstrap

## 本地快速启动

### 1. 准备 PostgreSQL

```bash
docker run -d \
  --name kb-ielts-pg \
  -e POSTGRES_DB=enterprise_kb \
  -e POSTGRES_USER=kb_user \
  -e POSTGRES_PASSWORD=changeme_dev_password \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. 配置环境变量

可以复制 `.env.example` 为 `.env`，或在终端中设置：

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/enterprise_kb
export SPRING_DATASOURCE_USERNAME=kb_user
export SPRING_DATASOURCE_PASSWORD=changeme_dev_password
```

`.env` 已被 `.gitignore` 忽略，不要提交真实密码。

### 3. 启动应用

```bash
mvn spring-boot:run
```

启动后访问：

- 首页：`http://localhost:8083/index.html`
- 今日学习：`http://localhost:8083/study.html`
- 健康检查：`http://localhost:8083/actuator/health`
- API 前缀：`/api/ielts`

## Docker Compose

完整容器化部署使用项目根目录的 `docker-compose.yml`，Compose 内部数据库名为 `ielts_db`，通过环境变量注入给应用：

```bash
PG_PASSWORD=changeme_strong_password docker compose up -d --build
```

Compose 为避免和本机 PostgreSQL 冲突，将数据库暴露在宿主机 `5433`，应用容器仍通过内部网络访问 `postgres:5432`。

## 常用命令

```bash
# 运行测试
mvn test

# 构建 jar
mvn package

# 查看健康状态
curl http://localhost:8083/actuator/health
```

更多部署和排障说明见 `docs/operation-ielts.md`。
