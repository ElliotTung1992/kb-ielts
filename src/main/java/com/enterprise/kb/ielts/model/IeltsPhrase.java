package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class IeltsPhrase {

    /** 主键 */
    private UUID id;
    /** 短语原文 */
    private String phrase;
    /** 中文含义 */
    private String meaningZh;
    /** 用法说明 */
    private String usageNote;
    /** 类型：signal-word / sentence-frame / collocation / connector / idiom */
    private String category;
    /** 难度（1基础 / 2中级 / 3高级） */
    private Integer difficulty;
    /** 适用技能，逗号分隔 */
    private String skillTags;
    /** 话题标签，逗号分隔 */
    private String topicTags;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
    /** 非列字段，由 LEFT JOIN ielts_study_records 填充；null=待学习 / LEARNING=学习中 / REVIEWING=复习中 / MASTERED=已掌握 */
    private String studyStatus;
    /** 非列字段，被几个技能内容引用；由 findAll 中的相关子查询填充 */
    private Integer linkCount;
    /** 非列字段，由 ielts_examples 表关联加载 */
    private List<IeltsExample> examples = new ArrayList<>();
}
