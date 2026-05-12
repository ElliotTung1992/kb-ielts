---
source_file: docs/design/class-diagram.md
ingested: 2026-05-11
---

# 类图摘要

原始文件：`docs/design/class-diagram.md`（含完整 Mermaid 类图）

## 核心要点

### 双模式 SpacedRepetitionCalculator
`SpacedRepetitionCalculator` 对不同内容类型使用不同算法：

| 模式 | 适用内容 | 方法 |
|---|---|---|
| 完整 SM-2 | `SM2_TYPES`（WORD / PHRASE）| `applySm2()` |
| 简化模式 | 其他所有内容类型 | `applySimple()` |

这意味着词汇类内容获得精细的间隔递增，非词汇内容则快速达到 MASTERED 状态。

### IeltsStudyConfig 每日配额
`dailyWords`, `dailyPhrases`, `dailyGrammar`, `dailyOthers` 分别配置，不是统一上限。

### SM-2 状态存储位置
SM-2 状态字段（`ease_factor`, `interval_days`, `repetition_count`, `next_review_at`）在 **`IeltsStudyRecord`** 上，不在 ReviewLog 上。  
`IeltsReviewLog` 只记录：`{recordId, rating, reviewedAt}`

### 评分体系
使用枚举文字：`AGAIN` / `HARD` / `GOOD` / `EASY`（不是 0-5 数字评分）

## 关键类

| 类 | 职责 |
|---|---|
| `SpacedRepetitionCalculator` | 静态工具类，`apply(record, rating)` 就地修改 record |
| `IeltsStudyService` | 接口，定义学习流程五大方法 |
| `IeltsStudyRecord` | 持有 SM-2 状态，UNIQUE(content_type, content_id) |
| `IeltsReviewLog` | 仅记录每次评分历史 |
| `IeltsStudyConfig` | Spring 配置Bean，注入每日配额 |
| `StudyPlanItem` | 工厂方法 `forNew()` / `forReview()` 区分新学/复习 |

## 相关 Wiki

- [[技术决策/ADR-002-间隔复习算法]]
- [[模块/间隔复习]]
- [[模块/学习计划]]
