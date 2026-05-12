---
source_file: docs/plan/todolist.md
ingested: 2026-05-11
---

# 待办事项摘要

原始文件：`docs/plan/todolist.md`

## 待实现：去掉「关联类型」(Problem 2)

将前端关联面板的分组改为按 `targetType` 而非 `linkType`：

**前端改动：**
- `js/links.js`：删除 `LINK_TYPE_LABELS`，按 `targetType` 分组（WORD→词汇 / PHRASE→短语 / PARAPHRASE→同义替换 / PRONUNCIATION→发音 / GRAMMAR_POINT→语法要点）
- `speaking/listening/reading/writing.html`：删除 Offcanvas 中的「关联类型」`<select>` 整块
- `study.html`：删除 `LINK_TYPE_ZH`，分组改为按 `targetType`

**后端改动：**
- `AddLinkRequest.java`：删除 `linkType` 字段
- `IeltsContentLinkServiceImpl.java`：删除 `link.setLinkType(...)` 
- 数据库/Mapper XML：INSERT 去掉 `link_type` 列（SELECT 可保留，不影响已有数据）

> [!note] 数据库列保留
> `link_type` 数据库列已存在，无需迁移，只是不再写入、不再用于分组展示。

## 待实现：关联管理内嵌进编辑 Modal (Problem 3)

去掉四大技能表中冗余文本字段，将关联管理从独立 Offcanvas 移入编辑/新增 Modal：

**数据库（新迁移 `005-drop-redundant-fields.sql`）：**
```sql
ALTER TABLE ielts_speaking_topics DROP COLUMN key_vocabulary, DROP COLUMN useful_phrases;
ALTER TABLE ielts_reading_items    DROP COLUMN key_vocabulary;
ALTER TABLE ielts_writing_tasks    DROP COLUMN key_phrases;
```

**后端：** 三个 Model + 三个 Mapper XML 删除对应字段

**前端核心改动：**
- `js/links.js` 新增 `LinksPanel.initInline(resource, id, containerId)` 方法
- 四个技能页 Modal 底部增加内嵌关联区（含搜索、添加、列表）
- 新建记录时关联区显示占位，保存后就地切换为编辑模式并激活关联区（两阶段流程）
- 删除四个技能页的独立「关联」按钮和 `linksOffcanvas`

## 相关 Wiki

- [[sources/plan-2026-0506]] — 关联功能原始设计
- [[架构设计/前端架构]]
