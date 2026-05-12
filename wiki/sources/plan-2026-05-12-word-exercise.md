---
type: source
origin: .planning/2026-05-12-word-exercise-mode/
ingested: 2026-05-12
---

# 规划会话：WORD/PHRASE 主动打字练习模式（2026-05-12）

## 目标

将 `study.html` 中 WORD / PHRASE 类型的复习方式从被动翻牌改为**主动打字练习**：展示中文例句 → 用户输入英文 → 逐词提示 → 揭示答案，全部例句完成后统一评分。

## 8 个实现阶段（全部完成）

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | CSS 样式扩充（`.ex-prompt-card`, `.hint-word` 等） | complete |
| Phase 2 | HTML 结构更新（`#wordExerciseArea` Variant A 卡片） | complete |
| Phase 3 | JS 状态变量（`exExamples/exCursor/exHintIdx/exWord/exPhonetic/exDef/EXERCISE_TYPES`） | complete |
| Phase 4 | `showItem()` 模式切换（exercise vs flip-card） | complete |
| Phase 5 | `renderCard()` 重构（EXERCISE_TYPES 提前 return） | complete |
| Phase 6 | 练习函数实现（`initExercise/renderExercise/exHint/revealAnswer/exNext`） | complete |
| Phase 7 | `flipCard()` 守卫（EXERCISE_TYPES 直接 return） | complete |
| Phase 8 | 今日计划限定为词汇类（WORD/PHRASE/PARAPHRASE，排除其余类型） | complete |

## 关键决策

- **UI Variant A**（卡片式，紫色渐变）— 从 `/prototype` 三个变体中用户选定
- `ielts_examples.translation` = 中文题目；`ielts_examples.sentence` = 英文答案
- 无例句的 WORD 不进入学习计划（后端 mapper 两处 EXISTS 过滤）
- PHRASE 与 WORD 逻辑一致，无需单独过滤
- 今日计划后端新增 `addToTodayPlan()` + `POST /api/ielts/study/today/items` 端点

## 修改文件

- `src/main/resources/static/css/ielts.css`
- `src/main/resources/static/study.html`
- `src/main/java/.../service/impl/IeltsStudyServiceImpl.java`
- `src/main/resources/mapper/IeltsStudyRecordMapper.xml`
- `src/main/resources/mapper/IeltsWordMapper.xml`
- `src/main/resources/mapper/IeltsDailyPlanItemMapper.xml`

## 相关页面

- [[模块/词汇系统]] — 练习模式的内容来源
- [[模块/学习计划]] — addToTodayPlan 端点、词汇类限定
- [[架构设计/前端架构]] — study.html 练习区 UI
- [[sources/prd-word-exercise-mode]] — 完整 PRD
