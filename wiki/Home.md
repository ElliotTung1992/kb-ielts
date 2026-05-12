# kb-ielts 开发知识库

雅思学习 Spring Boot 应用的架构设计与技术决策记录。

## 快速导航

### 架构设计
- [[架构设计/系统概览]] — 技术栈、整体架构、模块边界
- [[架构设计/分层架构]] — Controller / Service / Mapper / Model 分层规范
- [[架构设计/数据模型]] — 核心实体与数据库 Schema
- [[架构设计/API设计]] — REST API 规范与响应格式
- [[架构设计/前端架构]] — 静态 HTML + Bootstrap 页面体系

### 技术决策
- [[技术决策/ADR-001-技术栈选型]] — Spring Boot 3 + MyBatis + PostgreSQL
- [[技术决策/ADR-002-间隔复习算法]] — SM-2 变体与 SpacedRepetitionCalculator
- [[技术决策/ADR-003-数据库迁移]] — Liquibase changeset 管理策略
- [[技术决策/ADR-004-异常处理]] — 全局异常处理与错误码设计

### 模块
- [[模块/词汇系统]] — Words · Phrases · Paraphrase Groups
- [[模块/语法系统]] — Grammar Points · Exercises
- [[模块/技能模块]] — Listening · Speaking · Reading · Writing
- [[模块/学习计划]] — Daily Plan · Study Flow · 今日学习
- [[模块/间隔复习]] — Spaced Repetition · Review Log · 错题本
- [[模块/统计与仪表盘]] — Dashboard · Stats · Mock Test Trend

### 开发日志
- [[开发日志/索引]] — 历史迭代计划与决策记录

## 项目信息

| 项 | 值 |
|---|---|
| 技术栈 | Java 21 · Spring Boot 3.4.1 · MyBatis · PostgreSQL 16 |
| 构建 | Maven · Liquibase · Docker Compose |
| 前端 | 静态 HTML · Bootstrap · 原生 JS |
| 端口 | 8083 |
| API 前缀 | `/api/ielts` |
