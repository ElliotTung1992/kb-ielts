package com.enterprise.kb.ielts.dto;

/**
 * 学习统计数据响应
 */
public record StudyStatsResponse(
        /** 总学习记录数 */
        long totalRecords,
        /** 学习中数量 */
        long learningCount,
        /** 复习中数量 */
        long reviewingCount,
        /** 已掌握数量 */
        long masteredCount,
        /** 今日已复习次数 */
        long todayReviews,
        /** 连续学习天数 */
        int streakDays,
        /** 今日到期且未掌握的复习积压数 */
        long dueToday,
        /** 近 7 天复习次数 */
        long weeklyReviews,
        /** 近 30 天复习次数 */
        long monthlyReviews,
        /** 掌握率百分比，保留两位小数 */
        double masteryRate
) {}
