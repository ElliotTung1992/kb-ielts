package com.enterprise.kb.ielts.dto;

import com.enterprise.kb.ielts.model.IeltsMockTest;
import com.enterprise.kb.ielts.model.IeltsStudyProfile;

import java.util.List;

/**
 * 首页备考看板聚合响应。
 */
public record DashboardResponse(
        /** 备考档案 */
        IeltsStudyProfile profile,
        /** 今日学习计划 */
        TodayPlanResponse todayPlan,
        /** 学习统计摘要 */
        StudyStatsResponse stats,
        /** 当前建议优先训练的薄弱技能 */
        String weakestSkill,
        /** 最近高频错因 */
        List<MistakeStat> topMistakes,
        /** 最近一次模考记录 */
        IeltsMockTest latestMockTest,
        /** 下一步学习建议 */
        String nextRecommendation
) {}
