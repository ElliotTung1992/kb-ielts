package com.enterprise.kb.ielts.dto;

/**
 * 基于备考档案生成的每日计划建议。
 */
public record PlanSuggestionResponse(
        /** 距离考试天数；未设置考试日期时为 -1 */
        int daysToExam,
        /** 建议优先训练的技能 */
        String focusSkill,
        /** 建议每日新学单词数 */
        int dailyWords,
        /** 建议每日新学短语数 */
        int dailyPhrases,
        /** 建议每日阅读训练数量 */
        int dailyReadingItems,
        /** 建议每日写作训练数量 */
        int dailyWritingTasks,
        /** 给前端展示的建议说明 */
        String message
) {}
