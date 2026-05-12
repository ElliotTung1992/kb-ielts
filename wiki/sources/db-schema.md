---
source_file: docs/design/db-schema.md
ingested: 2026-05-11
---

# 数据库 ER 图摘要

原始文件：`docs/design/db-schema.md`（含完整 Mermaid ER 图）

## 设计文档覆盖的 14 张表

| 表 | 分类 |
|---|---|
| `ielts_words` | 内容 |
| `ielts_phrases` | 内容 |
| `ielts_paraphrase_groups` | 内容 |
| `ielts_pronunciation_points` | 内容（听力/口语）|
| `ielts_speaking_topics` | 内容（口语）|
| `ielts_listening_items` | 内容（听力）|
| `ielts_grammar_points` | 内容（语法）|
| `ielts_grammar_exercises` | 内容（语法）|
| `ielts_writing_tasks` | 内容（写作）|
| `ielts_reading_items` | 内容（阅读）|
| `ielts_examples` | 辅助（多态例句）|
| `ielts_study_records` | 学习状态（含SM-2字段）|
| `ielts_review_logs` | 复习历史 |
| `ielts_daily_plans` | 每日计划头 |

> 实际代码库中的表比设计文档多（见 `docs/design/db-schema.md` 为设计初版）

## 关联模式

**物理 FK（有级联）：**
- `ielts_grammar_exercises.grammar_point_id` → `ielts_grammar_points`
- `ielts_review_logs.record_id` → `ielts_study_records` ON DELETE CASCADE

**多态逻辑关联（无物理 FK）：**
- `ielts_examples(content_type, content_id)` — 关联五类内容（WORD/PHRASE/PARAPHRASE/PRONUNCIATION/GRAMMAR_POINT）
- `ielts_study_records(content_type, content_id)` — 关联全部可学内容类型（10类），UNIQUE约束

## SM-2 字段分布（重要）

```
ielts_study_records:
  ease_factor       DECIMAL  -- 初始 2.50（仅 WORD/PHRASE 有效）
  interval_days     INT      -- 当前间隔天数
  repetition_count  INT      -- 连续成功次数
  next_review_at    DATE     -- 下次复习日期
  status            VARCHAR  -- LEARNING / REVIEWING / MASTERED

ielts_review_logs:
  record_id         UUID FK
  rating            VARCHAR  -- AGAIN / HARD / GOOD / EASY
  reviewed_at       TIMESTAMPTZ
```

## 关键字段说明

- `ielts_words.frequency_level` — 雅思出现频率 1-5（5最高）
- `ielts_phrases.category` — signal-word/sentence-frame/collocation/connector/idiom
- `ielts_pronunciation_points.category` — stress/linking/weak-form/intonation/elision/assimilation
- `ielts_writing_tasks.task_number` — 1 或 2；`training_type` — ACADEMIC / GENERAL
- `ielts_reading_items.training_type` — ACADEMIC / GENERAL
- `ielts_daily_plans` — 无 plan_items 列（items 在独立表，设计文档未体现）

## 相关 Wiki

- [[架构设计/数据模型]]
- [[模块/间隔复习]]
