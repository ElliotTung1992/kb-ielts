---
type: source
origin: docs/plan/prd-word-exercise-mode.md
ingested: 2026-05-12
---

# PRD：WORD/PHRASE 主动打字练习模式

## 问题

翻牌模式是被动的（看英文想中文），无法训练主动产出英文的能力。另外，无例句的单词进入复习计划会导致空白体验。

## 方案摘要

对 WORD / PHRASE 类型改用**主动打字练习模式**：

1. 显示中文例句译文 + 中文释义（英文单词/音标隐藏）
2. 用户在文本框输入英文例句
3. 「提示」逐词揭示英文句子
4. 「查看答案」显示参考句 + 英文单词 + 音标
5. 多例句依次练习，全部完成后统一评分（AGAIN/HARD/GOOD/EASY）
6. 无例句的单词不进入计划，需先补充

视觉：渐变卡片（紫色），句子进度点内嵌卡片底部，评分区复用 `#ratingArea`。

## 实现决策

### 后端过滤

- `IeltsWordMapper.findNewContent` — `EXISTS (ielts_examples WHERE content_type='WORD')` 条件
- `IeltsStudyRecordMapper.findDueItemsWithSummary` — `NOT (content_type='WORD' AND NOT EXISTS examples)` 条件
- PHRASE 不需例句存在性过滤（依赖 `meaningZh` 不依赖 examples）

### 前端状态机

```
exExamples  — 例句数组（来自 content 对象）
exCursor    — 当前例句索引（0-based）
exHintIdx   — 已揭示提示词数量
exWord      — 英文单词/短语本体
exPhonetic  — 音标字符串
exDef       — 中文释义
EXERCISE_TYPES = Set(['WORD', 'PHRASE'])
```

核心函数：`initExercise(type, content)` → `renderExercise()` → `exHint()` / `revealAnswer()` / `exNext()`

### 数据字段映射

| 字段 | 用途 |
|------|------|
| `ielts_examples.translation` | 题目（中文，展示给用户） |
| `ielts_examples.sentence` | 参考答案（英文，答案揭示时显示） |

## 测试范围

- `IeltsWordMapper.findNewContent`（Mapper 集成测试）：有例句 → 返回；无例句 → 过滤；已有学习记录 → 不返回
- `IeltsStudyRecordMapper.findDueItemsWithSummary`：WORD+例句 → 返回；WORD 无例句 → 过滤
- 前端 JS 不在测试范围（无前端测试框架）

## 超出范围

- PHRASE 例句存在性过滤
- 自动评分 / 模糊匹配
- 练习历史统计
- 批量补例句入口

## 相关页面

- [[sources/plan-2026-05-12-word-exercise]] — 规划会话详情
- [[模块/词汇系统]]
- [[模块/学习计划]]
- [[架构设计/前端架构]]
