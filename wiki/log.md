# Ingest Log

## 2026-05-12 | batch ingest | .planning/ + docs/plan/prd-word-exercise-mode.md

- Sources: `.planning/2026-05-12-word-exercise-mode/task_plan.md`, `progress.md`, `docs/plan/prd-word-exercise-mode.md`
- Pages created: [[sources/plan-2026-05-12-word-exercise]], [[sources/prd-word-exercise-mode]]
- Pages updated: [[模块/词汇系统]], [[模块/学习计划]], [[架构设计/前端架构]], [[index]], [[hot]]
- Key insight: WORD/PHRASE 复习改为主动打字练习模式（中文→输入英文→逐词提示→答案）；今日计划限定为 VOCAB_TYPES（WORD/PHRASE/PARAPHRASE），排除所有技能类；无例句的单词在 mapper 层过滤不进计划。

## 2026-05-11 | batch ingest | docs/plan/ (4 sources)

- Sources: `docs/plan/plan-IELTS.md`, `plan-2026-0506.md`, `plan-2026-0508.md`, `todolist.md`
- Pages created: [[sources/plan-IELTS]], [[sources/plan-2026-0506]], [[sources/plan-2026-0508]], [[sources/todolist]]
- Pages updated: [[技术决策/ADR-002-间隔复习算法]], [[模块/学习计划]], [[模块/间隔复习]], [[架构设计/数据模型]], [[index]], [[hot]]
- Key insight: 非词汇类内容的"简化复习模式"在 2026-05-07 从"一次 GOOD/EASY 直接 MASTERED"改为阶段式间隔（3/7/14/30+天），同时新增 `ielts_daily_plan_items` 表固化每日计划条目解决刷新漂移问题。

## 2026-05-11 | batch ingest | docs/ (4 sources)

- Source: `docs/design/class-diagram.md`
- Source: `docs/design/db-schema.md`
- Source: `docs/design/sequence-study.md`
- Source: `docs/operation-ielts.md`
- Pages created: [[sources/class-diagram]], [[sources/db-schema]], [[sources/sequence-study]], [[sources/operation-ielts]], [[index]], [[hot]], [[log]]
- Pages updated: [[技术决策/ADR-002-间隔复习算法]], [[模块/间隔复习]]
- Key insight: SM-2 状态（ease_factor/interval/next_review_at）在 `ielts_study_records` 上，评分用 AGAIN/HARD/GOOD/EASY 枚举，WORD/PHRASE 走完整 SM-2，其他内容走简化模式（GOOD→MASTERED interval=365）。

## 2026-05-11 | scaffold | wiki 初始化

- Pages created: [[Home]], [[架构设计/系统概览]], [[架构设计/分层架构]], [[架构设计/数据模型]], [[架构设计/API设计]], [[架构设计/前端架构]], [[技术决策/ADR-001-技术栈选型]], [[技术决策/ADR-002-间隔复习算法]], [[技术决策/ADR-003-数据库迁移]], [[技术决策/ADR-004-异常处理]], [[模块/词汇系统]], [[模块/技能模块]], [[模块/学习计划]], [[模块/间隔复习]], [[模块/统计与仪表盘]], [[开发日志/索引]]
- Key insight: 初始 wiki 结构基于源码扫描建立，ADR-002 SM-2描述基于代码推断（后被 docs ingest 修正）。
