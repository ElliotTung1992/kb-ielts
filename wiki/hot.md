# Hot Cache

## 上次 Ingest（2026-05-11，第二批）

摄取了 `docs/plan/` 下 4 个文件：`plan-IELTS.md`、`plan-2026-0506.md`、`plan-2026-0508.md`、`todolist.md`

### 关键修正与新增（已更新 wiki）

1. **非词汇类 SM-2 模式改为阶段式**（ADR-002 + 间隔复习更新）：GOOD → 3/7/14/30+ 天递进，达到 rep≥4 且 interval≥30 才 MASTERED。原 sequence-study.md 设计文档中的"GOOD → 直接 MASTERED interval=365"已过时。

2. **ielts_daily_plan_items 表新增**（学习计划更新）：固化每日计划条目，解决刷新漂移问题。完成数按 plan_item.COMPLETED 统计，不再依赖 review_log 总数。

3. **SpacedRepetitionCalculator 升级为 Spring Bean**：注入 `Clock`（`ClockConfig` 提供），测试可用固定时间。

4. **ielts_review_logs 增加快照字段**：before/after 各字段，2026-05-07 加入。

5. **完整业务表清单**（数据模型更新）：比设计文档多出 `study_profile`、`mistake_logs`、`mock_tests`、`mock_test_sections`、`content_quality`、`writing_submissions`、`speaking_materials`、`topic_tags`、`daily_plan_items`、`content_links`。

6. **待办事项**（todolist）：去掉 linkType UI、将关联面板内嵌进编辑 Modal 的具体改动方案已记录。

### 当前已完成阶段

Phase 1-4、Phase 6 全部完成。Phase 5（种子数据）待完成。P0/P1 优化全部完成（截至 2026-05-07）。

最近一次 ingest 的快速上下文，供下次会话快速加载。

## 上次 Ingest（2026-05-11）

摄取了 `docs/` 下 4 个文件：`class-diagram.md`、`db-schema.md`、`sequence-study.md`、`operation-ielts.md`

### 关键修正（已更新 wiki）

1. **SM-2 状态存储位置**：在 `ielts_study_records`，不在 `ielts_review_logs`。`review_logs` 只存评分历史 `{record_id, rating, reviewed_at}`。

2. **评分枚举**：`AGAIN / HARD / GOOD / EASY`，不是数字 0-5。

3. **双模式算法**：`SpacedRepetitionCalculator` 对 `SM2_TYPES`（WORD/PHRASE）用完整 SM-2，其他内容用简化模式（GOOD/EASY→MASTERED interval=365，AGAIN/HARD→LEARNING interval=1）。

4. **每日配额**：`IeltsStudyConfig` 有 `dailyWords/dailyPhrases/dailyGrammar/dailyOthers` 四个独立配额。

5. **Docker Compose postgres 外部端口**：`5433`（不是 5432），避免与本地冲突。

### 已更新页面
- [[技术决策/ADR-002-间隔复习算法]] — 修正评分体系、SM-2参数存储位置
- [[模块/间隔复习]] — 同上

### 页面状态
- **架构设计/** — 5个页面，已覆盖全部系统结构
- **技术决策/** — 4个ADR，ADR-002已更新为正确的SM-2描述
- **模块/** — 5个模块页，间隔复习已更新
- **sources/** — 4个原始文档摘要
