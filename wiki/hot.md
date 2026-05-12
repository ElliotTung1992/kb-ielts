# Hot Cache

## 上次 Ingest（2026-05-12）

摄取了 `.planning/2026-05-12-word-exercise-mode/`（task_plan + progress）和 `docs/plan/prd-word-exercise-mode.md`

### 关键变更（已更新 wiki）

1. **WORD/PHRASE 改为主动打字练习模式**（词汇系统 + 前端架构更新）：
   - `#wordExerciseArea` 展示中文句子 → 用户打英文 → 逐词提示 → 揭示答案
   - `EXERCISE_TYPES = Set(['WORD','PHRASE'])`；其他类型仍用翻牌
   - `ielts_examples.translation` = 中文题目；`ielts_examples.sentence` = 英文答案

2. **无例句 WORD 过滤**：`IeltsWordMapper.findNewContent` 和 `IeltsStudyRecordMapper.findDueItemsWithSummary` 均加 `EXISTS (ielts_examples)` 条件

3. **今日计划限定词汇类**（学习计划更新）：`VOCAB_TYPES = {WORD, PHRASE, PARAPHRASE}`；SQL 已限定，Java 层不再过滤。技能类（LISTENING/READING/WRITING/SPEAKING/GRAMMAR_*）全部排除。

4. **`addToTodayPlan` 端点**：`POST /api/ielts/study/today/items`，幂等（ON CONFLICT DO NOTHING），冲突时不更新计划计数。

5. **单元测试**（`IeltsStudyServiceImplTest`）：3 个测试覆盖 review-before-new 排序、配额计算、空结果场景，全部通过。

### 待解决问题（来自 code review）

- `normalizeSummary` 无 summary 时回退为 `contentType:UUID`（对用户不友好）
- 缺少 `addToTodayPlan` 和 `POST /today/items` 的测试
- JS `renderExercise()` 空例句 guard 已修复（本次会话末尾）
- Service 冲突时 `syncPlanCounts` 已修复（本次会话末尾）

### 已更新页面

- [[模块/词汇系统]] — 练习模式说明 + 例句过滤规则
- [[模块/学习计划]] — 词汇类限定、addToTodayPlan 端点、配置字段更新
- [[架构设计/前端架构]] — study.html 练习区 DOM 结构 + JS 状态变量

---

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
