# kb-ielts — 运维操作文档

## 目录

1. [服务概述](#1-服务概述)
2. [本地调试（推荐）](#2-本地调试推荐)
3. [Docker Compose 部署](#3-docker-compose-部署)
4. [日志查看](#4-日志查看)
5. [健康检查](#5-健康检查)
6. [镜像管理](#6-镜像管理)
7. [常见问题](#7-常见问题)

---

## 1. 服务概述

`kb-ielts` 是独立的雅思学习 Spring Boot 应用，有自己的 PostgreSQL 和 docker-compose.yml，**不依赖 enterprise-kb 的 compose 栈**。

| 项目 | 值 |
|------|---|
| 应用端口 | `8083` |
| 健康检查端点 | `http://localhost:8083/actuator/health` |
| API 前缀 | `/api/ielts` |
| 前端入口 | `http://localhost:8083/index.html` |
| Docker Compose | `kb-ielts/docker-compose.yml` |

---

## 2. 本地调试（推荐）

本地 IDEA/Maven 直接启动时，使用独立的 PostgreSQL Docker 容器，**不需要完整的 compose 栈**。

### 2.1 启动 PostgreSQL 容器

```bash
docker run -d \
  --name kb-ielts-pg \
  -e POSTGRES_DB=enterprise_kb \
  -e POSTGRES_USER=kb_user \
  -e POSTGRES_PASSWORD=dge2026 \
  -p 5432:5432 \
  postgres:16-alpine
```

验证就绪：
```bash
docker exec kb-ielts-pg pg_isready -U kb_user -d enterprise_kb
```

### 2.2 启动应用

在 IDEA 中直接运行 `IeltsApplication`，或：

```bash
cd /Users/ganendong/Documents/workspace/claude2/kb-ielts
mvn spring-boot:run
```

Liquibase 会在首次启动时自动建表并执行全部迁移脚本。

### 2.3 停止与重启 PostgreSQL

```bash
# 停止（数据保留）
docker stop kb-ielts-pg

# 下次重启
docker start kb-ielts-pg

# 彻底清除数据
docker rm -v kb-ielts-pg
```

### 2.4 数据库连接信息

| 项目 | 值 |
|------|---|
| Host | `localhost:5432` |
| Database | `kb_ielts` |
| Username | `kb_user` |
| Password | `dge2026` |

> 以上值为 `application.yml` 中的默认值，可通过环境变量 `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` 覆盖。

---

## 3. Docker Compose 部署

`kb-ielts/docker-compose.yml` 包含 `postgres` + `app` 两个服务，适合完整容器化部署。

> **注意**：compose 中的数据库配置（`ielts_db` / `ielts_user`）与 `application.yml` 默认值不同，compose 通过环境变量覆盖注入，二者不冲突。

所有命令均在 `kb-ielts` 目录执行：

```bash
cd /Users/ganendong/Documents/workspace/claude2/kb-ielts
```

**首次启动（需要 `PG_PASSWORD` 环境变量）：**
```bash
PG_PASSWORD=your_password docker compose up -d
```

**只启动 postgres（不启动应用）：**
```bash
PG_PASSWORD=your_password docker compose up -d postgres
```

**停止服务：**
```bash
docker compose stop
```

**停止并移除容器：**
```bash
docker compose down
```

**停止并清除数据卷：**
```bash
docker compose down -v
```

### 3.1 Compose 服务说明

| 服务名 | 容器名 | 说明 |
|--------|--------|------|
| `postgres` | `ielts-postgres` | PostgreSQL 16，数据卷 `postgres_data`，外部端口 `5433` |
| `app` | `kb-ielts` | Spring Boot 应用，依赖 postgres healthy 后启动 |

> Compose 中 postgres 外部端口为 `5433`（避免与本地已有 postgres 冲突），应用通过内部网络 `ielts-network` 访问 postgres。

---

## 4. 日志查看

**本地运行时**：日志直接输出到控制台。

**Docker Compose 运行时：**

```bash
# 实时跟踪应用日志
docker logs -f kb-ielts

# 查看最近 100 行
docker logs --tail 100 kb-ielts

# 通过 compose 查看（支持多服务）
docker compose logs -f app

# 查看 postgres 日志
docker compose logs -f postgres
```

---

## 5. 健康检查

```bash
# 查看容器状态
docker ps | grep kb-ielts

# 直接调用健康端点
curl -s http://localhost:8083/actuator/health | python3 -m json.tool
```

状态说明（Docker 部署时）：
- `health: starting` — 容器启动中，Spring Boot 冷启动约需 30-60 秒
- `healthy` — 服务就绪
- `unhealthy` — 服务异常，查看日志排查

---

## 6. 镜像管理

**重新构建镜像并启动（代码有变更时）：**
```bash
cd /Users/ganendong/Documents/workspace/claude2/kb-ielts
PG_PASSWORD=your_password docker compose up -d --build app
```

**清理悬空镜像：**
```bash
docker rmi $(docker images -f "dangling=true" -q)
```

---

## 7. 常见问题

**Liquibase 迁移报错**

通常是上一次迁移异常中断导致 `DATABASECHANGELOGLOCK` 卡死：
```sql
-- 连接数据库后执行
DELETE FROM databasechangeloglock;
```

**端口 8083 被占用**
```bash
lsof -i :8083
```
找到占用进程后 kill，或修改 `application.yml` 中的 `server.port`。

**本地调试时连接数据库失败**

确认 `kb-ielts-pg` 容器在运行：
```bash
docker ps | grep kb-ielts-pg
docker exec kb-ielts-pg pg_isready -U kb_user -d enterprise_kb
```

**Docker Compose 启动失败**

`PG_PASSWORD` 为必填环境变量，未设置时报错：`PG_PASSWORD is required`。
