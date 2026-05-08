package com.enterprise.kb.ielts.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 今日学习计划响应
 */
public record TodayPlanResponse(
        /** 计划日期 */
        LocalDate planDate,
        /** 今日计划总数（到期复习 + 补充新内容） */
        int totalItems,
        /** 已完成数 */
        int completedItems,
        /** 计划条目列表：studyMode=REVIEW 的排在前，NEW 的排在后 */
        List<StudyPlanItem> items
) {}
