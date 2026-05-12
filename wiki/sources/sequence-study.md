---
source_file: docs/design/sequence-study.md
ingested: 2026-05-11
---

# 学习流程时序图摘要

原始文件：`docs/design/sequence-study.md`（含完整 Mermaid 时序图）

## 四个核心接口

### 1. GET /api/ielts/study/today — 获取今日计划

```
PlanMapper.findByDate(today)
  ├── null → 生成新计划：
  │     ├── RecMapper.findDueForReview(today)  ← 到期复习优先
  │     ├── CMapper.fetchSummaries(reviewItems)
  │     └── CMapper.findNewContent(type, limit) ← 按配额补充新内容
  │         (dailyWords / dailyPhrases / dailyGrammar / dailyOthers)
  └── 存在 → 直接返回
返回: TodayPlanResponse{planDate, totalItems, completedItems, items[]}
```

### 2. POST /api/ielts/study/start — 开始学习

```
RecMapper.findByContentTypeAndId(type, id)
  ├── null (NEW) → insert: status=LEARNING, easeFactor=2.50, intervalDays=1, nextReviewAt=明天
  └── 存在 → 直接返回
返回: IeltsStudyRecord
```

### 3. POST /api/ielts/study/review — 提交复习评分

```
RecMapper.findById(recordId)
SM2.apply(record, rating)  ← 就地修改 record
RecMapper.update(record)
LogMapper.insert({recordId, rating, now()})
PlanMapper.incrementCompleted(today)
返回: 更新后的 IeltsStudyRecord
```

### 4. GET /api/ielts/study/stats — 查看统计

```
RecMapper.countByStatus()         → {LEARNING:N, REVIEWING:N, MASTERED:N}
LogMapper.countLast30Days()       → [{date, count} × 30]
LogMapper.getStudyDates()         → 历史学习日期（计算 streak）
返回: StudyStatsResponse{streak, todayCompleted, statusCounts, last30Days}
```

## SM-2 算法细节（时序图注释提取）

### WORD / PHRASE — 完整 SM-2

| 评分 | interval | ease_factor | status |
|---|---|---|---|
| AGAIN | 重置为 1 | ef - 0.20 | LEARNING |
| HARD | × 1.2 | ef - 0.15 | — |
| GOOD | 标准递增 | — | rep≥5 且 interval≥21 → MASTERED |
| EASY | × 1.3 | ef + 0.15 | — |

### 其他内容 — 简化模式

| 评分 | 结果 |
|---|---|
| GOOD / EASY | status=MASTERED, interval=365 |
| AGAIN / HARD | status=LEARNING, interval=1 |

## 相关 Wiki

- [[技术决策/ADR-002-间隔复习算法]]
- [[模块/学习计划]]
- [[模块/间隔复习]]
