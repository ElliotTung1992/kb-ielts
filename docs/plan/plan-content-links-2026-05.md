# 四大技能关联跨技能内容 — 设计规划

> 排期：2026-05

---

## 一、背景与目标

当前四大技能内容表（听力/阅读/写作/口语）与跨技能内容表（单词/短语/同义替换/发音/语法）之间没有显式关联。`skill_tags` 只能标注"这个单词适用于听力"，但无法表达"这道听力题考的就是这个同义替换"。

**目标**：建立技能内容与跨技能内容的多对多关联，支持：
- 学习听力题时，同步推送题中涉及的单词/同义替换
- 学习一个单词时，看到它出现在哪些听力/阅读题里
- 今日计划基于关联关系附带配套内容

---

## 二、关联场景分析

| 技能内容 | 可关联的跨技能内容 | 典型用途 |
|---------|-----------------|---------|
| 听力题 `LISTENING` | 单词 `WORD` | 题目中出现的关键生词 |
| 听力题 `LISTENING` | 短语 `PHRASE` | 听力信号词，如 *however / on the other hand* |
| 听力题 `LISTENING` | 同义替换 `PARAPHRASE` | 核心考点：听到 A 写 B |
| 听力题 `LISTENING` | 发音 `PRONUNCIATION` | 涉及连读/弱读规则的例句 |
| 阅读题 `READING` | 单词 `WORD` | 文章生词/高频词 |
| 阅读题 `READING` | 短语 `PHRASE` | 文章中的固定搭配 |
| 阅读题 `READING` | 同义替换 `PARAPHRASE` | 判断题/匹配题的改写考点 |
| 写作题 `WRITING` | 单词 `WORD` | 话题核心词汇 |
| 写作题 `WRITING` | 短语 `PHRASE` | 高分句式/写作搭配 |
| 写作题 `WRITING` | 同义替换 `PARAPHRASE` | 避免重复用词的替换方案 |
| 写作题 `WRITING` | 语法要点 `GRAMMAR_POINT` | 范文涉及的语法结构 |
| 口语话题 `SPEAKING` | 单词 `WORD` | 话题高分词汇 |
| 口语话题 `SPEAKING` | 短语 `PHRASE` | 口语连接词/固定表达 |
| 口语话题 `SPEAKING` | 发音 `PRONUNCIATION` | 话题常见发音难点 |
| 口语话题 `SPEAKING` | 语法要点 `GRAMMAR_POINT` | 口语中适用的语法结构 |

---

## 三、数据库设计

新增一张关联表，用多态多对多表达所有关系，避免为每种组合单独建表。

### `ielts_content_links`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID PK | 主键 |
| `source_type` | VARCHAR(30) | 来源技能内容类型：`LISTENING` / `READING` / `WRITING` / `SPEAKING` |
| `source_id` | UUID | 来源内容主键 |
| `target_type` | VARCHAR(30) | 目标跨技能内容类型：`WORD` / `PHRASE` / `PARAPHRASE` / `PRONUNCIATION` / `GRAMMAR_POINT` |
| `target_id` | UUID | 目标内容主键 |
| `link_type` | VARCHAR(30) | 关联类型：`vocabulary` 词汇 / `paraphrase` 同义替换考点 / `grammar` 语法应用 / `pronunciation` 发音规律 / `signal` 信号词 |
| `note` | TEXT | 补充说明，如"第3题的同义替换考点" |
| `created_at` | TIMESTAMPTZ | 创建时间 |

**约束**：`UNIQUE(source_type, source_id, target_type, target_id)`
**索引**：`idx_links_source(source_type, source_id)`、`idx_links_target(target_type, target_id)`

### DDL

```sql
CREATE TABLE ielts_content_links (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_type VARCHAR(30) NOT NULL CHECK (source_type IN ('LISTENING','READING','WRITING','SPEAKING')),
    source_id   UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL CHECK (target_type IN ('WORD','PHRASE','PARAPHRASE','PRONUNCIATION','GRAMMAR_POINT')),
    target_id   UUID NOT NULL,
    link_type   VARCHAR(30) CHECK (link_type IN ('vocabulary','paraphrase','grammar','pronunciation','signal')),
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (source_type, source_id, target_type, target_id)
);
CREATE INDEX idx_links_source ON ielts_content_links (source_type, source_id);
CREATE INDEX idx_links_target ON ielts_content_links (target_type, target_id);
```

---

## 四、API 设计

路径约定：`/api/ielts/{skill-resource}/{id}/links`

### 4.1 查询关联内容（含跨技能详情）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/listening-items/{id}/links` | 返回该听力题关联的所有跨技能内容，含详情快照 |
| `GET` | `/reading-items/{id}/links` | 同上，阅读题 |
| `GET` | `/writing-tasks/{id}/links` | 同上，写作题 |
| `GET` | `/speaking-topics/{id}/links` | 同上，口语话题 |

响应结构：
```json
[
  {
    "linkId": "...",
    "linkType": "paraphrase",
    "targetType": "PARAPHRASE",
    "targetId": "...",
    "note": "第3题同义替换考点",
    "target": {
      "groupName": "increase",
      "coreExpression": "increase",
      "synonyms": "rise\ngrow\ngo up"
    }
  }
]
```

### 4.2 反向查询（跨技能内容出现在哪些技能内容中）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/words/{id}/links` | 该单词出现在哪些听力/阅读/写作/口语内容中 |
| `GET` | `/paraphrase-groups/{id}/links` | 该替换组是哪些题目的考点 |
| `GET` | `/grammar-points/{id}/links` | 该语法点在哪些写作/口语题中应用 |
| `GET` | `/phrases/{id}/links` | 该短语出现在哪些技能内容中 |
| `GET` | `/pronunciation-points/{id}/links` | 该发音要点在哪些技能内容中引用 |

### 4.3 管理关联

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/listening-items/{id}/links` | 添加关联；body：`{targetType, targetId, linkType, note}` |
| `DELETE` | `/listening-items/{id}/links/{linkId}` | 删除单条关联 |
| `POST` | `/reading-items/{id}/links` | 同上，阅读题 |
| `DELETE` | `/reading-items/{id}/links/{linkId}` | — |
| `POST` | `/writing-tasks/{id}/links` | 同上，写作题 |
| `DELETE` | `/writing-tasks/{id}/links/{linkId}` | — |
| `POST` | `/speaking-topics/{id}/links` | 同上，口语话题 |
| `DELETE` | `/speaking-topics/{id}/links/{linkId}` | — |

---

## 五、后端模块结构

```
新增：
├── model/
│   └── IeltsContentLink.java
├── mapper/
│   └── IeltsContentLinkMapper.java
├── service/
│   ├── IeltsContentLinkService.java
│   └── impl/IeltsContentLinkServiceImpl.java
├── controller/
│   └── IeltsContentLinkController.java    ← 统一处理四个技能的 links 子路由
├── dto/
│   └── ContentLinkDto.java                ← 含 target 详情快照的响应体
└── resources/mapper/
    └── IeltsContentLinkMapper.xml

新增迁移：
└── db/changelog/004-create-content-links.sql
```

---

## 六、前端页面规划

### 6.1 交互方案

所有页面目前是**列表 + 编辑 Modal** 的两层结构，引入关联后需要新增交互层。

| 方案 | 描述 | 问题 |
|------|------|------|
| 合并进编辑 Modal | 在编辑表单底部加关联区 | 编辑 Modal 已经很长；职责混淆 |
| 新建详情页 | 点击跳转独立详情页 | 打断列表浏览流；成本高 |
| **独立关联 Offcanvas** | 点击按钮从右侧滑入 Offcanvas | 不覆盖列表；职责清晰；Bootstrap 原生支持 ✅ |

**选择**：独立的关联管理 Offcanvas，与编辑 Modal 完全分离。

### 6.2 技能内容页（听力 / 阅读 / 写作 / 口语）

**操作列变化：**
```
原来：[编辑] [加入学习] [删除]
现在：[编辑] [关联🔗] [加入学习] [删除]
```

**关联管理 Offcanvas 布局：**

```
┌──────────────────────────────────────────┐
│ 🔗 《机场问询 Section 1》关联内容   [×]  │
├──────────────────────────────────────────┤
│ ┌ 添加关联 ──────────────────────────┐  │
│ │ 内容类型  [同义替换        ▼]      │  │
│ │ 搜索      [increase_____________]  │  │
│ │           ┌──────────────────────┐ │  │
│ │           │ increase → rise/surge│ │  │
│ │           │ increase → go up     │ │  │
│ │           └──────────────────────┘ │  │
│ │ 关联类型  [同义替换考点    ▼]      │  │
│ │ 备注      [第3题答案位置_______]   │  │
│ │                           [添加]   │  │
│ └────────────────────────────────────┘  │
├──────────────────────────────────────────┤
│ 同义替换考点  2                          │
│  ├ increase → rise/surge  [×]           │
│  └ expensive → costly     [×]           │
│                                          │
│ 词汇  3                                  │
│  ├ storage  n. 存储        [×]           │
│  ├ fee      n. 费用        [×]           │
│  └ luggage  n. 行李        [×]           │
│                                          │
│ 语法要点  0                              │
│   暂无关联                               │
└──────────────────────────────────────────┘
```

**添加关联的搜索交互：**
1. 选择「内容类型」（单词 / 短语 / 同义替换 / 发音 / 语法要点）
2. 在搜索框输入关键词，实时调用对应 list API（防抖 300ms），下拉展示最多 10 条结果
3. 点击选中目标条目
4. 选择「关联类型」，填写备注（可选）
5. 点击「添加」调用 `POST /{skill-resource}/{id}/links`

**关联列表展示规则：**
- 按 `link_type` 分组，每组显示条数
- 每条显示目标内容摘要（单词原形 / 核心表达 / 语法标题）+ 备注
- `[×]` 就地删除该行，不刷新整个列表

**分组标题与 link_type 对应：**

| 分组标题 | link_type | 主要适用 |
|---------|-----------|--------|
| 同义替换考点 | `paraphrase` | 听力、阅读 |
| 词汇 | `vocabulary` | 全部 |
| 信号词 | `signal` | 听力 |
| 语法应用 | `grammar` | 写作、口语 |
| 发音规律 | `pronunciation` | 听力、口语 |

### 6.3 跨技能内容页（单词 / 短语 / 同义替换 / 发音 / 语法要点）

**操作列变化：**
```
原来：[编辑] [加入学习] [删除]
现在：[编辑] [引用↩N] [加入学习] [删除]
```

`↩N` 角标显示引用总数，`N=0` 时按钮置灰。

**反向引用 Offcanvas 布局（只读）：**

```
┌──────────────────────────────────────────┐
│ ↩ "increase" 被引用的位置        [×]    │
├──────────────────────────────────────────┤
│ 听力题  1                                │
│  └ 机场问询 Section 1  [同义替换考点]    │
│                                          │
│ 阅读题  2                                │
│  ├ Climate Change  [同义替换考点]        │
│  └ Global Trade    [词汇]               │
│                                          │
│ 写作题  1                                │
│  └ 柱状图-交通方式变化  [词汇]           │
│                                          │
│ 口语话题  0                              │
│   暂无引用                               │
└──────────────────────────────────────────┘
```

按技能类型分组，每条显示标题 + 关联类型标签 + 备注。**只读**，删除关联从技能内容页操作。

### 6.4 各页面改动清单

| 页面 | 改动 |
|------|------|
| `listening.html` | 操作列加「关联」按钮；底部加关联管理 Offcanvas |
| `reading.html` | 同上 |
| `writing.html` | 同上 |
| `speaking.html` | 同上 |
| `words.html` | 操作列加「引用↩N」按钮；底部加反向引用 Offcanvas |
| `phrases.html` | 同上 |
| `paraphrases.html` | 同上 |
| `pronunciation.html` | 同上 |
| `grammar-points.html` | 同上 |

### 6.5 新增 JS 模块

| 文件 | 职责 |
|------|------|
| `js/links.js` | 关联管理 Offcanvas 通用逻辑：加载、搜索、添加、删除；技能内容页复用 |
| `js/backlinks.js` | 反向引用 Offcanvas 通用逻辑：按技能类型分组展示；跨技能内容页复用 |

### 6.6 `js/api.js` 新增方法

```js
Api.links = {
  list:      (resource, id)              => GET    `/api/ielts/${resource}/${id}/links`,
  add:       (resource, id, body)        => POST   `/api/ielts/${resource}/${id}/links`,
  remove:    (resource, id, linkId)      => DELETE `/api/ielts/${resource}/${id}/links/${linkId}`,
  backlinks: (resource, id)             => GET    `/api/ielts/${resource}/${id}/links`,
}
```

---

## 七、一对多关系的关键细节

### 7.1 添加时的重复检测

后端有 `UNIQUE(source_type, source_id, target_type, target_id)` 约束，前端收到 409 时提示「该关联已存在」，不报错崩溃。

### 7.2 关联列表的懒加载

技能内容页列表不预先加载关联数，只有点击「关联」按钮时才请求。
原因：列表 20 条若每条预加载会产生 20 个额外请求。

### 7.3 反向引用数量（避免 N+1）

跨技能内容页「引用↩N」的数量，在 list API 响应中直接附带 `linkCount` 字段（后端 LEFT JOIN COUNT，单次查询），不逐行单独请求。

### 7.4 删除内容时的级联提示

删除一个单词/听力题前，若存在关联记录，确认弹窗提示：
> 「该条目有 N 条关联记录，删除后关联将一并移除，确认继续？」

后端 `ielts_content_links` 无物理 FK（多态关联），需在 Service 层删除内容前先清理关联记录。

---

## 八、study.html 翻卡页扩展

今日学习翻卡背面，在答案/解析下方增加**配套内容区**：

```
┌─────────────────────────────────────┐
│  [正面] 听力题标题 / 单词            │
├─────────────────────────────────────┤
│  [背面] 答案 / 释义 / 解析           │
│                                     │
│  ─── 相关内容 ───────────────────  │
│  同义替换: increase → rise/surge    │
│  词汇: storage / fee / luggage      │
│  [查看详情 →]                       │
│                                     │
│  [Again]  [Hard]  [Good]  [Easy]    │
└─────────────────────────────────────┘
```

`StudyPlanItem` 扩展 `linkedItems: List<ContentLinkDto>` 字段，由 `getTodayPlan()` 返回时附带关联内容快照（仅摘要），前端直接渲染，无需额外请求。

---

## 九、开发阶段

### Phase 6 — 内容关联

- [ ] `004-create-content-links.sql` Liquibase 迁移
- [ ] `IeltsContentLink` Model + Mapper XML
- [ ] `IeltsContentLinkService`：增删查（正向 + 反向）+ `listWithLinkCount`（供 list API 附带引用数）
- [ ] `IeltsContentLinkController`：四个技能的 `/links` 子路由 + 五个跨技能内容的反向查询
- [ ] `getTodayPlan()` 扩展：`StudyPlanItem` 附带 `linkedItems` 快照
- [ ] `js/links.js`：关联管理 Offcanvas 通用逻辑
- [ ] `js/backlinks.js`：反向引用 Offcanvas 通用逻辑
- [ ] `js/api.js`：新增 `Api.links` 方法
- [ ] 技能内容页（4 个）：加「关联」按钮 + Offcanvas
- [ ] 跨技能内容页（5 个）：加「引用↩N」按钮 + Offcanvas
- [ ] `study.html`：翻卡背面加配套内容区
