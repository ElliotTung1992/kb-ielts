package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsReadingItem {

    /** 主键 */
    private UUID id;
    /** 文章标题 */
    private String title;
    /** 考试类型：ACADEMIC / GENERAL */
    private String trainingType;
    /** 难度（1基础 / 2中级 / 3高级） */
    private Integer difficulty;
    /** 文章正文 */
    private String passageText;
    /** 题型：tfng / ynng / matching-headings / matching-info / sentence-completion / multiple-choice / summary-completion / short-answer */
    private String questionType;
    /** 题目，换行分隔 */
    private String questions;
    /** 答案及定位解析 */
    private String answers;
    /** 该题型阅读技巧 */
    private String tips;
    /** 话题标签，逗号分隔 */
    private String topicTags;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
    /** 非列字段，由 LEFT JOIN ielts_study_records 填充；null=待学习 / LEARNING=学习中 / REVIEWING=复习中 / MASTERED=已掌握 */
    private String studyStatus;
}
