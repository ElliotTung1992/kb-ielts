# Task Plan: WORD/PHRASE 主动打字练习模式

## Goal
将 study.html 中 WORD 和 PHRASE 类型的学习方式从被动翻牌改为主动打字练习（中文 → 输入英文），逐句练习，全部完成后统一评分。

## Current Phase
Complete

## Phases

### Phase 1: CSS 样式扩充
- **Status:** complete

### Phase 2: HTML 结构更新
- **Status:** complete

### Phase 3: JS 状态变量
- **Status:** complete

### Phase 4: showItem() 模式切换
- **Status:** complete

### Phase 5: renderCard() 重构
- **Status:** complete

### Phase 6: 练习函数实现
- **Status:** complete

### Phase 7: flipCard() 守卫
- **Status:** complete

### Phase 8: 今日计划限制为词汇类（追加）
- 今日计划只保留 WORD / PHRASE / PARAPHRASE，排除其余所有类型
- IeltsStudyServiceImpl 新增 VOCAB_TYPES 常量，reviewItems 过滤，移除非词汇 fetchNewItems 调用
- **Status:** complete

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| UI Variant A 卡片式 | 用户从 prototype 三选一选定 |
| examples.translation = 题目 | 显示中文让用户写英文 |
| 单词级评分（所有例句完成后） | SM-2 以单词为粒度 |
| PHRASE 与 WORD 逻辑一致 | 统一体验 |
| 后端 mapper 过滤已完成 | 无例句的单词不进学习计划 |

## Errors Encountered
| Error | Resolution |
|-------|------------|

## Files to Modify
- src/main/resources/static/css/ielts.css
- src/main/resources/static/study.html
