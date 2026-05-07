# kb-ielts 模块开发计划

## 一、模块定位

`kb-ielts` 是**完全独立的 Spring Boot 应用**，不依赖 `kb-common` 外部模块（公共类内置在 `com.enterprise.kb.common` 包下）。

- **独立进程**：有自己的 main class、`application.yml`、端口（**8083**）、Liquibase 迁移
- **无认证、无权限校验**，所有接口直接开放
- **单学习者模式**：不引入用户体系，所有学习记录为全局唯一，无 `user_id` 概念
- 与现有知识库（kb-user、kb-document、kb-search 等）完全解耦
- 独立 Maven 项目，`spring-boot-starter-parent` 作为父 pom

**依赖关系**：独立可执行 JAR，无外部模块依赖

---

## 二、核心功能

### 2.1 数据内容管理（按四技能设计）

内容按雅思四项技能分类，单词和短语作为跨技能的基础资源，通过 `skill_tags` 标注适用范围。

**统一学习状态筛选**：所有十类内容的列表接口均支持 `?studyStatus=` 查询参数，实现按学习进度过滤：

| 参数值 | 含义 | 实现方式 |
|--------|------|----------|
| 不传 | 返回所有内容（不按状态过滤） | 无 WHERE 条件 |
| `NEW` | 尚未开始学习的内容 | `ielts_study_records` 中无对应记录（`sr.id IS NULL`） |
| `LEARNING` | 学习中（初次接触） | `sr.status = 'LEARNING'` |
| `REVIEWING` | 复习中（间隔复习阶段） | `sr.status = 'REVIEWING'` |
| `MASTERED` | 已掌握 | `sr.status = 'MASTERED'` |

实现架构：`findAll` 和 `countAll` SQL 通过 `LEFT JOIN ielts_study_records sr ON sr.content_type = '...' AND sr.content_id = t.id` 关联，并将 `sr.status` 以 `study_status` 列名返回，由 Model 类中的非列字段 `studyStatus` 自动映射（`autoMapping="true"` + `map-underscore-to-camel-case: true`）。

**统一难度字段**：所有十类内容表均包含 `difficulty` 字段，取值 1-3，用于每日学习计划的分层和筛选：

| 难度值 | 标签 | 说明 |
|--------|------|------|
| 1 | 基础 | 高频核心内容，入门必学 |
| 2 | 中级 | 常见进阶内容，备考重点（默认值） |
| 3 | 高级 | 低频难点，冲高分必备 |

**统一例句存储**：各内容表不再内联 `example_sentence`/`example_translation`，改为统一存入 `ielts_examples` 表，按 `(content_type, content_id)` 关联，支持每条内容对应多个例句。

| 内容类型 | 所属技能 | 说明 |
|----------|----------|------|
| 核心单词 `ielts_words` | 跨技能 | 词形、音标、词性、中英释义、频率级别、词表来源、适用技能标签、近义/关联词 |
| 常用短语 `ielts_phrases` | 跨技能 | 短语原文、中文含义、适用技能（听力信号词 / 口语连接词 / 写作句型等） |
| 同义替换组 `ielts_paraphrase_groups` | 跨技能 | 核心表达 + 全部同义替换词/短语 + 用法区别 |
| 发音要点 `ielts_pronunciation_points` | 听力 / 口语 | 连读 / 弱读 / 重音 / 语调 / 省音规则讲解、例词例句、常见错误 |
| 语法要点 `ielts_grammar_points` | 写作 / 口语 | 语法分类、中英文讲解、规则总结、常见错误 |
| 语法练习 `ielts_grammar_exercises` | 写作 / 口语 | 填空 / 改错 / 句子转换 / 选择题，关联具体语法要点 |
| 口语话题 `ielts_speaking_topics` | Speaking | Part 1/2/3 题目，参考思路，高分词汇 |
| 听力练习 `ielts_listening_items` | Listening | Section 1-4，题型（填空/选择/配对/地图），语境描述，题目与答案，作答技巧 |
| 阅读练习 `ielts_reading_items` | Reading | 文章段落（学术/普通），题型（判断题/配对/句子填空等），题目与答案解析 |
| 写作题目 `ielts_writing_tasks` | Writing | Task 1（图表/信件）/ Task 2（议论文），范文，评分要点，常用句式 |

### 2.2 批量导入

- 支持 JSON 文件批量导入所有十类内容
- 导入时自动去重（单词按 `word`、短语按 `phrase` 唯一键 upsert；其余内容按 `title` 去重）
- 返回导入结果：成功数、跳过数、失败明细

### 2.3 每日学习计划

- 系统每天生成学习计划（可在配置文件中设置每日单词数、短语数、口语话题数）
- 计划来源：优先安排复习到期的内容，不足则从未学过的内容中补充
- 学习状态流转：`NEW` → `LEARNING` → `REVIEWING` → `MASTERED`
- **注意**：`NEW` 是隐式状态，表示 `ielts_study_records` 中尚无该内容的记录。用户通过 `POST /study/start` 将某条 NEW 内容加入学习，系统写入一条 `status = 'LEARNING'` 的记录；`NEW` 状态本身不占数据库行

### 2.4 间隔复习（SM-2 算法）

复习间隔基于熟练度系数（`ease_factor`）动态计算，默认初始间隔序列：

| 复习次数 | 下次复习间隔 |
|----------|-------------|
| 第 1 次  | 1 天        |
| 第 2 次  | 3 天        |
| 第 3 次  | 7 天        |
| 第 4 次  | 15 天       |
| 第 5 次+ | 间隔 × ease_factor（默认 2.5） |

每次复习标记熟练程度（Again / Hard / Good / Easy），系统据此调整 `ease_factor` 和下次复习时间。

### 2.5 学习统计

- 每日新学 / 复习数量
- 连续学习天数（streak）
- 各状态内容数量分布（NEW / LEARNING / REVIEWING / MASTERED）
- 近 30 天学习趋势

---

## 三、数据库设计

### 3.1 `ielts_words`（雅思核心单词，跨技能）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `word` | VARCHAR(100) UNIQUE | 单词原形 |
| `phonetic_uk` | VARCHAR(100) | 英式音标 |
| `phonetic_us` | VARCHAR(100) | 美式音标 |
| `part_of_speech` | VARCHAR(20) | 词性（n./v./adj. 等） |
| `definition_zh` | TEXT | 中文释义（多义词换行分隔） |
| `definition_en` | TEXT | 英文释义 |
| `frequency_level` | SMALLINT | 雅思出现频率（1-5，5最高） |
| `word_list` | VARCHAR(50) | 词表来源：AWL / GSL / IELTS |
| `difficulty` | SMALLINT | 难度（1基础 / 2中级 / 3高级，默认 2） |
| `skill_tags` | VARCHAR(100) | 适用技能，逗号分隔（listening,reading,writing,speaking） |
| `topic_tags` | VARCHAR(255) | 话题标签，逗号分隔（如 environment,health） |
| `related_words` | TEXT | 近义词 / 关联词（换行分隔） |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | `findAll` 时通过 LEFT JOIN `ielts_study_records` 填充；`null`=NEW / `LEARNING` / `REVIEWING` / `MASTERED` |
| `examples` | *(非列字段)* | 关联查询 `ielts_examples` 中 `content_type='WORD'` 的记录 |

> 注：`example_sentence` / `example_translation` 已迁移至 `ielts_examples` 表并从本表删除。

### 3.2 `ielts_phrases`（常用短语，跨技能）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `phrase` | VARCHAR(300) UNIQUE | 短语原文 |
| `meaning_zh` | TEXT | 中文含义 |
| `usage_note` | TEXT | 用法说明 |
| `category` | VARCHAR(50) | 类型：signal-word / sentence-frame / collocation / connector / idiom |
| `difficulty` | SMALLINT | 难度（1-3，默认 2） |
| `skill_tags` | VARCHAR(100) | 适用技能 |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |
| `examples` | *(非列字段)* | 关联 `ielts_examples`（content_type='PHRASE'） |

> 注：`example_sentence` / `example_translation` 已迁移至 `ielts_examples` 表。

### 3.3 `ielts_paraphrase_groups`（同义替换组 — 跨技能）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `group_name` | VARCHAR(200) | 组名（通常为核心概念，如 "increase"） |
| `core_expression` | VARCHAR(200) | 核心表达 |
| `synonyms` | TEXT | 全部同义替换词/短语，每行一条 |
| `usage_note` | TEXT | 用法区别说明 |
| `difficulty` | SMALLINT | 难度（1-3，默认 2） |
| `skill_tags` | VARCHAR(100) | 适用技能 |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |
| `examples` | *(非列字段)* | 关联 `ielts_examples`（content_type='PARAPHRASE'），sentence=原句，translation=改写句 |

> 注：`example_original` / `example_paraphrased` 已迁移至 `ielts_examples` 表。

### 3.4 `ielts_pronunciation_points`（发音要点 — 听力 / 口语）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(200) | 要点标题 |
| `category` | VARCHAR(50) | stress / linking / weak-form / intonation / elision / assimilation |
| `explanation_zh` | TEXT | 中文讲解 |
| `rule_summary` | TEXT | 规则要点（换行分隔） |
| `common_mistakes` | TEXT | 常见错误及纠正 |
| `skill_tags` | VARCHAR(100) | 适用技能（listening,speaking） |
| `difficulty` | SMALLINT | 难度（1-3） |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |
| `examples` | *(非列字段)* | 关联 `ielts_examples`（content_type='PRONUNCIATION'） |

> 注：`examples` 列已迁移至 `ielts_examples` 表。

### 3.5 `ielts_grammar_points`（语法要点 — Grammar）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(200) | 语法要点标题 |
| `category` | VARCHAR(50) | tense / conditional / passive / relative-clause / modal / comparison / article / sentence-structure |
| `explanation_zh` | TEXT | 中文讲解 |
| `explanation_en` | TEXT | 英文讲解 |
| `key_rules` | TEXT | 核心规则提炼（换行分隔） |
| `common_errors` | TEXT | 常见错误及纠正 |
| `difficulty` | SMALLINT | 难度（1-3） |
| `skill_tags` | VARCHAR(100) | 适用技能（writing,speaking） |
| `topic_tags` | VARCHAR(255) | 相关话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |
| `examples` | *(非列字段)* | 关联 `ielts_examples`（content_type='GRAMMAR_POINT'） |

> 注：`examples` 列已迁移至 `ielts_examples` 表。

### 3.6 `ielts_grammar_exercises`（语法练习 — Grammar）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `grammar_point_id` | UUID FK | 关联 ielts_grammar_points（可为 null，综合练习） |
| `question_type` | VARCHAR(30) | fill-in-blank / error-correction / sentence-transformation / multiple-choice |
| `question` | TEXT | 题目 |
| `options` | TEXT | 选项（仅 multiple-choice，换行分隔） |
| `answer` | TEXT | 标准答案 |
| `explanation` | TEXT | 解析 |
| `difficulty` | SMALLINT | 难度（1-3） |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |

### 3.7 `ielts_speaking_topics`（口语话题 — Speaking）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(200) | 话题标题 |
| `part` | SMALLINT | 口语考试 Part（1/2/3） |
| `question` | TEXT | 具体问题 |
| `reference_answer` | TEXT | 参考答案思路或范例 |
| `key_vocabulary` | TEXT | 高分词汇（换行分隔） |
| `useful_phrases` | TEXT | 常用句式（换行分隔） |
| `difficulty` | SMALLINT | 难度（1-3，默认 2） |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |

### 3.8 `ielts_listening_items`（听力练习 — Listening）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(200) | 练习标题 |
| `section` | SMALLINT | Section 编号（1-4） |
| `question_type` | VARCHAR(50) | form-completion / note-completion / table-completion / multiple-choice / matching / map-labelling / flow-chart |
| `context_description` | TEXT | 听力场景描述 |
| `script_excerpt` | TEXT | 关键脚本片段 |
| `questions` | TEXT | 题目（换行分隔） |
| `answers` | TEXT | 答案及解析 |
| `tips` | TEXT | 题型专项技巧 |
| `difficulty` | SMALLINT | 难度（1-3） |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |

### 3.9 `ielts_reading_items`（阅读练习 — Reading）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(300) | 文章标题 |
| `training_type` | VARCHAR(20) | ACADEMIC / GENERAL |
| `difficulty` | SMALLINT | 难度（1-3） |
| `passage_text` | TEXT | 文章正文 |
| `question_type` | VARCHAR(50) | tfng / ynng / matching-headings / matching-info / sentence-completion / multiple-choice / summary-completion / short-answer |
| `questions` | TEXT | 题目 |
| `answers` | TEXT | 答案及定位解析 |
| `key_vocabulary` | TEXT | 核心词汇及释义 |
| `tips` | TEXT | 阅读技巧 |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |

### 3.10 `ielts_writing_tasks`（写作题目 — Writing）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `title` | VARCHAR(200) | 题目简短描述 |
| `task_number` | SMALLINT | Task 编号（1 或 2） |
| `training_type` | VARCHAR(20) | ACADEMIC / GENERAL |
| `task_type` | VARCHAR(50) | Task 1：bar-chart / line-graph / pie-chart / table / diagram / map / letter；Task 2：argument / discussion / problem-solution / two-part |
| `prompt` | TEXT | 完整题目要求 |
| `image_description` | TEXT | Task 1 图表数据描述 |
| `model_answer` | TEXT | 范文 |
| `band_score_note` | TEXT | 高分答案评分要点 |
| `key_phrases` | TEXT | 常用句式（换行分隔） |
| `difficulty` | SMALLINT | 难度（1-3，默认 2） |
| `topic_tags` | VARCHAR(255) | 话题标签 |
| `created_at` | TIMESTAMPTZ | — |
| `updated_at` | TIMESTAMPTZ | — |
| `study_status` | *(非列字段)* | LEFT JOIN 填充 |

### 3.11 `ielts_examples`（统一例句表）

各内容类型的例句统一存储，不再内联于内容表。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `content_type` | VARCHAR(30) | WORD / PHRASE / PARAPHRASE / PRONUNCIATION / GRAMMAR_POINT |
| `content_id` | UUID | 对应内容 ID |
| `sentence` | TEXT | 例句（英文）/ 原句 |
| `translation` | TEXT | 例句翻译（中文）/ 改写句 |
| `note` | TEXT | 补充说明 |
| `sort_order` | INT | 排序序号（默认 0） |
| `created_at` | TIMESTAMPTZ | — |

**索引**：`(content_type, content_id)`

### 3.12 `ielts_study_records`（学习记录，每条内容唯一）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `content_type` | VARCHAR(30) | `WORD` / `PHRASE` / `PARAPHRASE` / `PRONUNCIATION` / `GRAMMAR_POINT` / `GRAMMAR_EXERCISE` / `SPEAKING` / `LISTENING` / `READING` / `WRITING` |
| `content_id` | UUID | 对应内容 ID |
| `status` | VARCHAR(20) | `LEARNING` / `REVIEWING` / `MASTERED` |
| `ease_factor` | DECIMAL(4,2) | 难易系数（初始 2.50） |
| `interval_days` | INT | 当前复习间隔天数 |
| `repetition_count` | INT | 累计复习次数 |
| `next_review_at` | DATE | 下次复习日期 |
| `last_reviewed_at` | TIMESTAMPTZ | 最后复习时间 |
| `created_at` | TIMESTAMPTZ | 首次加入学习时间 |

**唯一约束**：`(content_type, content_id)`

### 3.13 `ielts_review_logs`（每次复习操作日志）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `record_id` | UUID FK | 关联 ielts_study_records（ON DELETE CASCADE） |
| `rating` | VARCHAR(10) | `AGAIN` / `HARD` / `GOOD` / `EASY` |
| `reviewed_at` | TIMESTAMPTZ | 复习时间 |

### 3.14 `ielts_daily_plans`（每日学习计划，每天唯一）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `plan_date` | DATE UNIQUE | 计划日期 |
| `total_items` | INT | 计划学习总数 |
| `completed_items` | INT | 已完成数（默认 0） |
| `generated_at` | TIMESTAMPTZ | 计划生成时间 |

---

## 四、API 设计

所有接口路径前缀：`/api/ielts`，**无需认证，直接访问**。

### 4.1 单词管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/words` | 分页查询单词；支持 `?difficulty` / `?wordList` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/words/{id}` | 获取单词详情（含关联 examples） |
| `POST` | `/words` | 新增单词 |
| `PUT` | `/words/{id}` | 更新单词 |
| `DELETE` | `/words/{id}` | 删除单词 |
| `POST` | `/words/batch` | 批量导入（JSON 数组） |

### 4.2 短语管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/phrases` | 分页查询短语；支持 `?difficulty` / `?category` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/phrases/{id}` | 短语详情（含关联 examples） |
| `POST` | `/phrases` | 新增 |
| `PUT` | `/phrases/{id}` | 更新 |
| `DELETE` | `/phrases/{id}` | 删除 |
| `POST` | `/phrases/batch` | 批量导入 |

### 4.3 同义替换管理（Paraphrase）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/paraphrase-groups` | 分页查询；支持 `?difficulty` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/paraphrase-groups/{id}` | 替换组详情（含关联 examples） |
| `POST` | `/paraphrase-groups` | 新增 |
| `PUT` | `/paraphrase-groups/{id}` | 更新 |
| `DELETE` | `/paraphrase-groups/{id}` | 删除 |
| `POST` | `/paraphrase-groups/batch` | 批量导入 |

### 4.4 发音要点管理（Pronunciation）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/pronunciation-points` | 分页查询；支持 `?difficulty` / `?category` / `?studyStatus` 筛选 |
| `GET` | `/pronunciation-points/{id}` | 发音要点详情（含关联 examples） |
| `POST` | `/pronunciation-points` | 新增 |
| `PUT` | `/pronunciation-points/{id}` | 更新 |
| `DELETE` | `/pronunciation-points/{id}` | 删除 |
| `POST` | `/pronunciation-points/batch` | 批量导入 |

### 4.5 语法管理（Grammar）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/grammar-points` | 分页查询语法要点；支持 `?difficulty` / `?category` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/grammar-points/{id}` | 语法要点详情（含关联 examples） |
| `POST` | `/grammar-points` | 新增语法要点 |
| `PUT` | `/grammar-points/{id}` | 更新语法要点 |
| `DELETE` | `/grammar-points/{id}` | 删除语法要点（级联删除关联练习题） |
| `POST` | `/grammar-points/batch` | 批量导入语法要点 |
| `GET` | `/grammar-exercises` | 分页查询语法练习；支持 `?difficulty` / `?questionType` / `?grammarPointId` / `?studyStatus` 筛选 |
| `GET` | `/grammar-exercises/{id}` | 练习题详情 |
| `POST` | `/grammar-exercises` | 新增练习题 |
| `PUT` | `/grammar-exercises/{id}` | 更新练习题 |
| `DELETE` | `/grammar-exercises/{id}` | 删除练习题 |
| `POST` | `/grammar-exercises/batch` | 批量导入练习题 |

### 4.6 口语话题管理（Speaking）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/speaking-topics` | 分页查询；支持 `?difficulty` / `?part` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/speaking-topics/{id}` | 话题详情 |
| `POST` | `/speaking-topics` | 新增 |
| `PUT` | `/speaking-topics/{id}` | 更新 |
| `DELETE` | `/speaking-topics/{id}` | 删除 |
| `POST` | `/speaking-topics/batch` | 批量导入 |

### 4.7 听力练习管理（Listening）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/listening-items` | 分页查询；支持 `?difficulty` / `?section` / `?questionType` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/listening-items/{id}` | 练习详情 |
| `POST` | `/listening-items` | 新增 |
| `PUT` | `/listening-items/{id}` | 更新 |
| `DELETE` | `/listening-items/{id}` | 删除 |
| `POST` | `/listening-items/batch` | 批量导入 |

### 4.8 阅读练习管理（Reading）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/reading-items` | 分页查询；支持 `?difficulty` / `?trainingType` / `?questionType` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/reading-items/{id}` | 练习详情 |
| `POST` | `/reading-items` | 新增 |
| `PUT` | `/reading-items/{id}` | 更新 |
| `DELETE` | `/reading-items/{id}` | 删除 |
| `POST` | `/reading-items/batch` | 批量导入 |

### 4.9 写作题目管理（Writing）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/writing-tasks` | 分页查询；支持 `?difficulty` / `?taskNumber` / `?trainingType` / `?topicTags` / `?studyStatus` 筛选 |
| `GET` | `/writing-tasks/{id}` | 题目详情 |
| `POST` | `/writing-tasks` | 新增 |
| `PUT` | `/writing-tasks/{id}` | 更新 |
| `DELETE` | `/writing-tasks/{id}` | 删除 |
| `POST` | `/writing-tasks/batch` | 批量导入 |

### 4.10 学习计划与复习

路径前缀：`/api/ielts/study`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/today` | 获取今日学习计划（到期复习优先，不足补充新内容）；返回 `TodayPlanResponse` |
| `POST` | `/start` | 将 NEW 内容加入学习记录（写入 `status=LEARNING`）；入参 `StartStudyRequest{contentType, contentId}` |
| `POST` | `/review` | 提交复习结果；入参 `ReviewRequest{recordId, rating}` |
| `GET` | `/stats` | 学习统计；返回 `StudyStatsResponse`（streak、按技能状态分布、近30天趋势） |
| `GET` | `/records` | 全部学习记录；支持 `content_type` / `status` 筛选、分页 |

### 4.11 导入格式说明（JSON）

所有内容统一使用 JSON 批量导入，字段与数据库字段对应（camelCase）。

**单词示例**：
```json
[
  {
    "word": "abandon",
    "phoneticUk": "/əˈbændən/",
    "phoneticUs": "/əˈbændən/",
    "partOfSpeech": "v.",
    "definitionZh": "放弃；抛弃",
    "definitionEn": "to leave someone or something permanently",
    "frequencyLevel": 4,
    "wordList": "AWL",
    "skillTags": "reading,writing",
    "topicTags": "environment,society",
    "relatedWords": "desert\nabandon\nforsake"
  }
]
```

**听力练习示例**：
```json
[
  {
    "title": "机场问询 Section 1",
    "section": 1,
    "questionType": "form-completion",
    "contextDescription": "一名旅客向机场服务台工作人员咨询行李寄存事宜。",
    "scriptExcerpt": "Staff: ...the storage fee is five pounds per item per day...",
    "questions": "1. Storage fee per item: £_____ per day\n2. Payment method accepted: _____",
    "answers": "1. five / 5\n2. credit card",
    "tips": "Section 1 为双人对话，答案通常直接读出，注意数字和拼写。",
    "difficulty": 1,
    "topicTags": "travel,airport"
  }
]
```

---

## 五、模块内部结构

```
kb-ielts/
├── pom.xml                          ← spring-boot-starter-parent，可独立打包运行
└── src/main/
    ├── java/com/enterprise/kb/
    │   ├── common/                      ← 公共类（内置，不依赖外部模块）
    │   │   ├── dto/
    │   │   │   ├── ApiResponse.java
    │   │   │   └── PageResponse.java
    │   │   └── exception/
    │   │       ├── KbException.java
    │   │       ├── ResourceExistException.java
    │   │       └── ResourceNotFoundException.java
    │   └── ielts/
    │       ├── IeltsApplication.java        ← @SpringBootApplication 独立启动入口
    │       ├── config/
    │       │   └── IeltsStudyConfig.java    ← @ConfigurationProperties(prefix="enterprise.ielts.study")
    │       ├── controller/
    │       │   ├── IeltsWordController.java
    │       │   ├── IeltsPhraseController.java
    │       │   ├── IeltsParaphraseGroupController.java
    │       │   ├── IeltsPronunciationPointController.java
    │       │   ├── IeltsGrammarPointController.java
    │       │   ├── IeltsGrammarExerciseController.java
    │       │   ├── IeltsSpeakingTopicController.java
    │       │   ├── IeltsListeningItemController.java
    │       │   ├── IeltsReadingItemController.java
    │       │   ├── IeltsWritingTaskController.java
    │       │   └── IeltsStudyController.java
    │       ├── service/
    │       │   ├── IeltsWordService.java
    │       │   ├── IeltsPhraseService.java
    │       │   ├── IeltsParaphraseGroupService.java
    │       │   ├── IeltsPronunciationPointService.java
    │       │   ├── IeltsGrammarPointService.java
    │       │   ├── IeltsGrammarExerciseService.java
    │       │   ├── IeltsSpeakingTopicService.java
    │       │   ├── IeltsListeningItemService.java
    │       │   ├── IeltsReadingItemService.java
    │       │   ├── IeltsWritingTaskService.java
    │       │   ├── IeltsStudyService.java
    │       │   └── impl/
    │       │       └── （各 ServiceImpl 实现类）
    │       ├── mapper/
    │       │   ├── IeltsWordMapper.java
    │       │   ├── IeltsPhraseMapper.java
    │       │   ├── IeltsParaphraseGroupMapper.java
    │       │   ├── IeltsPronunciationPointMapper.java
    │       │   ├── IeltsGrammarPointMapper.java
    │       │   ├── IeltsGrammarExerciseMapper.java
    │       │   ├── IeltsSpeakingTopicMapper.java
    │       │   ├── IeltsListeningItemMapper.java
    │       │   ├── IeltsReadingItemMapper.java
    │       │   ├── IeltsWritingTaskMapper.java
    │       │   ├── IeltsExampleMapper.java
    │       │   ├── IeltsStudyRecordMapper.java
    │       │   ├── IeltsReviewLogMapper.java
    │       │   └── IeltsDailyPlanMapper.java
    │       ├── model/
    │       │   ├── IeltsWord.java
    │       │   ├── IeltsPhrase.java
    │       │   ├── IeltsParaphraseGroup.java
    │       │   ├── IeltsPronunciationPoint.java
    │       │   ├── IeltsGrammarPoint.java
    │       │   ├── IeltsGrammarExercise.java
    │       │   ├── IeltsSpeakingTopic.java
    │       │   ├── IeltsListeningItem.java
    │       │   ├── IeltsReadingItem.java
    │       │   ├── IeltsWritingTask.java
    │       │   ├── IeltsExample.java
    │       │   ├── IeltsStudyRecord.java
    │       │   ├── IeltsReviewLog.java
    │       │   └── IeltsDailyPlan.java
    │       ├── dto/
    │       │   ├── StudyPlanItem.java       ← 今日计划单个条目（studyMode=NEW|REVIEW）
    │       │   ├── TodayPlanResponse.java   ← 今日计划响应（record）
    │       │   ├── StartStudyRequest.java   ← 开始学习新内容入参（record）
    │       │   ├── ReviewRequest.java       ← 提交复习结果入参
    │       │   └── StudyStatsResponse.java  ← 统计数据
    │       ├── exception/
    │       │   └── IeltsExceptionHandler.java  ← @RestControllerAdvice 全局异常处理
    │       ├── study/
    │       │   └── SpacedRepetitionCalculator.java  ← SM-2 间隔计算
    │       └── typehandler/
    │           └── UUIDTypeHandler.java     ← MyBatis UUID 类型处理器
    └── resources/
        ├── application.yml
        ├── db/changelog/
        │   ├── db.changelog-master.xml
        │   ├── 001-create-ielts-tables.sql      ← 13 张内容表 + 学习记录表 DDL
        │   ├── 002-create-ielts-examples.sql    ← 新增 ielts_examples 表，迁移各内容表例句数据
        │   └── 003-add-word-related-words.sql   ← ielts_words 新增 related_words 列
        ├── mapper/
        │   └── （各 Mapper XML，共 14 个）
        └── static/
            ├── index.html               ← 首页 / 仪表盘
            ├── study.html               ← 今日学习（翻卡复习）
            ├── words.html               ← 单词管理
            ├── phrases.html             ← 短语管理
            ├── paraphrases.html         ← 同义替换组管理
            ├── pronunciation.html       ← 发音要点管理
            ├── grammar-points.html      ← 语法要点管理
            ├── grammar-exercises.html   ← 语法练习管理
            ├── speaking.html            ← 口语话题
            ├── listening.html           ← 听力练习
            ├── reading.html             ← 阅读练习
            ├── writing.html             ← 写作题目
            └── stats.html               ← 学习统计
                assets/
                ├── css/
                │   └── ielts.css
                └── js/
                    ├── api.js           ← fetch 封装，BASE_URL=/api/ielts
                    └── nav.js           ← 侧边栏高亮、通用初始化
```

---

## 六、开发阶段完成情况

### Phase 1 — 模块骨架 + 数据层 ✅

- [x] 创建独立 `kb-ielts` Maven 项目，`pom.xml` 使用 `spring-boot-starter-parent`
- [x] 编写 `IeltsApplication.java`（独立 main class，端口 8083）
- [x] 编写 `application.yml`（含 PostgreSQL、MyBatis、PageHelper、每日学习配置）
- [x] Liquibase 迁移：`001-create-ielts-tables.sql`（13 张内容表 + 学习记录相关表）
- [x] Liquibase 迁移：`002-create-ielts-examples.sql`（例句统一表 + 数据迁移）
- [x] Liquibase 迁移：`003-add-word-related-words.sql`（单词近义词列）
- [x] 实现全部 Model 类、Mapper 接口、Mapper XML（基础 CRUD）
- [x] `UUIDTypeHandler`（MyBatis UUID 类型处理器）
- [x] `IeltsStudyConfig`（配置属性绑定）
- [x] `IeltsExceptionHandler`（全局异常处理）

### Phase 2 — 内容管理 API ✅

- [x] 十类内容的 Service + Controller（CRUD + 分页 + 筛选）
- [x] 批量导入功能（`POST /batch`）

### Phase 3 — 学习核心逻辑 ✅

- [x] `SpacedRepetitionCalculator`（SM-2 算法）
- [x] `IeltsStudyService.getTodayPlan()`（幂等生成今日计划）
- [x] `IeltsStudyService.startStudy()`（NEW 内容加入学习记录）
- [x] `IeltsStudyService.submitReview()`（更新记录 + 写日志 + 更新完成数）
- [x] `IeltsStudyService.getStats()`（streak、状态分布、近30天趋势）

### Phase 4 — 前端页面 ✅

- [x] `assets/css/ielts.css`
- [x] `assets/js/api.js`（fetch 封装，BASE_URL = /api/ielts）
- [x] `assets/js/nav.js`
- [x] 全部 HTML 页面（index / study / words / phrases / paraphrases / pronunciation / grammar-points / grammar-exercises / speaking / listening / reading / writing / stats）

### Phase 5 — 种子数据（待完成）

- [ ] 准备各类内容 JSON 种子文件
- [ ] 通过批量导入接口初始化数据
- [ ] 端到端验证

---

## 七、关键设计决策

| 决策点 | 选择 | 原因 |
|--------|------|------|
| 启动方式 | 独立 Spring Boot 进程（端口 8083） | 与知识库主应用完全解耦 |
| 认证方案 | 无认证 | 纯个人学习工具 |
| 用户维度 | 单学习者，无 user_id | 逻辑最简 |
| 例句存储 | 独立 `ielts_examples` 表 | 支持多例句；避免各内容表字段膨胀；统一查询接口 |
| SRS 算法 | SM-2 简化版 | 逻辑可控，无外部依赖 |
| NEW 状态 | 隐式（无 study_records 行） | 减少无意义行；通过 `POST /study/start` 显式加入学习 |
| 导入幂等性 | 以 `word` / `phrase` / `title` 唯一键 upsert | 避免重复，支持数据修正 |
| 内容与进度分离 | 内容表存共享数据，`study_records` 存进度 | 利于独立维护 |
| 公共类 | 内置于模块（`com.enterprise.kb.common`） | 独立部署，无外部模块依赖 |
