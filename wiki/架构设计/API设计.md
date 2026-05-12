# API 设计

## 基础规范

- 前缀：`/api/ielts`
- 格式：JSON
- 统一响应体：`ApiResponse<T>` (`common/dto/ApiResponse.java`)

```json
{
  "success": true,
  "data": { ... },
  "message": "ok"
}
```

分页响应：`PageResponse<T>`

```json
{
  "list": [...],
  "total": 100,
  "pageNum": 1,
  "pageSize": 20
}
```

## 路由约定

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/{module}` | 列表（支持分页 ?page=&size=）|
| GET | `/{module}/{id}` | 详情 |
| POST | `/{module}` | 新建 |
| PUT | `/{module}/{id}` | 更新 |
| DELETE | `/{module}/{id}` | 删除 |

## 主要路由

| 前缀 | Controller | 说明 |
|---|---|---|
| `/api/ielts/words` | IeltsWordController | 词汇 |
| `/api/ielts/phrases` | IeltsPhraseController | 短语 |
| `/api/ielts/paraphrase-groups` | IeltsParaphraseGroupController | 同义替换 |
| `/api/ielts/pronunciation` | IeltsPronunciationPointController | 发音 |
| `/api/ielts/grammar/points` | IeltsGrammarPointController | 语法点 |
| `/api/ielts/grammar/exercises` | IeltsGrammarExerciseController | 语法练习 |
| `/api/ielts/listening` | IeltsListeningItemController | 听力 |
| `/api/ielts/speaking/topics` | IeltsSpeakingTopicController | 口语话题 |
| `/api/ielts/speaking/materials` | IeltsSpeakingMaterialController | 口语素材 |
| `/api/ielts/reading` | IeltsReadingItemController | 阅读 |
| `/api/ielts/writing/tasks` | IeltsWritingTaskController | 写作题目 |
| `/api/ielts/writing/submissions` | IeltsWritingSubmissionController | 作文提交 |
| `/api/ielts/study` | IeltsStudyController | 学习流程（计划/复习/记录）|
| `/api/ielts/dashboard` | IeltsDashboardController | 仪表盘数据 |
| `/api/ielts/mistakes` | IeltsMistakeController | 错题 |
| `/api/ielts/mock-tests` | IeltsMockTestController | 模拟测试 |
| `/api/ielts/links` | IeltsContentLinkController | 内容关联 |
| `/api/ielts/topic-tags` | IeltsTopicTagController | 话题标签 |
| `/api/ielts/training` | IeltsTrainingController | 专项训练 |
| `/api/ielts/profile` | IeltsStudyProfileController | 学习配置 |

## 错误码

| HTTP | 场景 |
|---|---|
| 400 | 请求参数校验失败 |
| 404 | 资源不存在（ResourceNotFoundException）|
| 409 | 资源已存在（ResourceExistException）|
| 500 | 业务异常（KbException）|
