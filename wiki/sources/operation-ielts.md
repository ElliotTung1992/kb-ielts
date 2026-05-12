---
source_file: docs/operation-ielts.md
ingested: 2026-05-11
---

# 运维操作文档摘要

原始文件：`docs/operation-ielts.md`

## 快速参考

| 项 | 值 |
|---|---|
| 应用端口 | `8083` |
| 健康检查 | `http://localhost:8083/actuator/health` |
| 本地数据库端口 | `5432` |
| Compose postgres 外部端口 | `5433`（避免与本地冲突）|
| Compose 内部网络 | `ielts-network` |

## 本地调试（推荐方式）

```bash
# 1. 启动 PostgreSQL
docker run -d --name kb-ielts-pg \
  -e POSTGRES_DB=enterprise_kb -e POSTGRES_USER=kb_user \
  -e POSTGRES_PASSWORD=changeme_dev_password -p 5432:5432 \
  postgres:16-alpine

# 2. 启动应用
mvn spring-boot:run
```

## Docker Compose

```bash
# 完整启动（需要 PG_PASSWORD）
PG_PASSWORD=your_password docker compose up -d

# 重建镜像
PG_PASSWORD=your_password docker compose up -d --build app
```

| Compose 服务 | 容器名 | 说明 |
|---|---|---|
| `postgres` | `ielts-postgres` | 外部端口 5433，数据卷 `postgres_data` |
| `app` | `kb-ielts` | 依赖 postgres healthy 后启动 |

## 常见问题速查

| 问题 | 解决 |
|---|---|
| Liquibase 锁死 | `DELETE FROM databasechangeloglock;` |
| 端口 8083 占用 | `lsof -i :8083` 找到进程后 kill |
| 连接数据库失败 | `docker ps | grep kb-ielts-pg` 确认容器运行 |
| compose 启动失败 | 检查是否设置 `PG_PASSWORD` 环境变量 |

## 相关 Wiki

- [[架构设计/系统概览]]
