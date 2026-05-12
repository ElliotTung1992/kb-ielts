---
source_file: docs/plan/plan-IELTS.md
ingested: 2026-05-11
---

# 主开发计划摘要

原始文件：`docs/plan/plan-IELTS.md`（700+ 行，含完整 DB Schema、API 和阶段完成情况）

## 系统定位

- 独立 Spring Boot 进程，端口 8083
- **无认证**，无权限校验，所有接口直接开放
- **单学习者模式**，无 `user_id`
- 独立 Maven 项目，无外部模块依赖

## 关键设计决策（section 七）

| 决策 | 选择 | 原因 |
|---|---|---|
| NEW 状态 | **隐式**（无 study_records 行） | 减少无意义行；`POST /study/start` 显式加入学习 |
| 例句存储 | 独立 `ielts_examples` 表 | 支持多例句；避免各内容表字段膨胀 |
| SRS 算法 | SM-2 简化版 | 逻辑可控，无外部依赖 |
| 导入幂等性 | 以 `word`/`phrase`/`title` 唯一键 upsert | 避免重复，支持数据修正 |
| 内容关联建模 | 多态多对多（单表 `ielts_content_links`） | 避免为 20 种组合各建关联表 |
| linkCount N+1 | `findAll` SQL 内嵌 correlated subquery | 单次查询附带引用数 |
| 关联级联删除 | 服务层手动清理（无物理 FK） | 多态关联无法用 ON DELETE CASCADE |

## 全量 Changelog 文件

所有变更已合并至 `001-kb-ielts-schema.sql`（原 001-008 合并），后续新表走独立文件：

| Changeset | 内容 |
|---|---|
| `ielts:001-words` 至 `ielts:014-daily-plans` | 14 张核心表 |
| `ielts:015` 至 | 关联表、business feature 表 |
| `009-create-topic-tags.sql` | 话题标签表（独立文件）|

## 六大开发阶段完成情况

| Phase | 内容 | 状态 |
|---|---|---|
| 1 | 模块骨架 + 数据层（Model/Mapper/XML） | ✅ |
| 2 | 十类内容管理 API（CRUD + 分页 + 批量导入） | ✅ |
| 3 | 学习核心逻辑（计划生成 + SM-2 + 统计） | ✅ |
| 4 | 前端页面（全部 HTML + JS + CSS） | ✅ |
| 5 | 种子数据（待完成）| ⬜ |
| 6 | 内容关联（ielts_content_links + 前端 Offcanvas）| ✅ |

## 重要特性：studyStatus 虚拟字段

所有十类内容列表接口均支持 `?studyStatus=NEW/LEARNING/REVIEWING/MASTERED`：
- `NEW` — study_records 中无记录（`sr.id IS NULL`）
- `LEARNING/REVIEWING/MASTERED` — `sr.status = '...'`
- 实现：`findAll` SQL 通过 `LEFT JOIN ielts_study_records` 关联，`study_status` 列自动映射到 Model 非列字段

## 相关 Wiki

- [[架构设计/数据模型]]
- [[模块/学习计划]]
- [[技术决策/ADR-001-技术栈选型]]
- [[sources/plan-2026-0508]]
