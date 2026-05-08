package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.dto.DashboardResponse;
import com.enterprise.kb.ielts.dto.MistakeStat;
import com.enterprise.kb.ielts.dto.StudyStatsResponse;
import com.enterprise.kb.ielts.dto.TodayPlanResponse;
import com.enterprise.kb.ielts.mapper.IeltsMistakeLogMapper;
import com.enterprise.kb.ielts.mapper.IeltsMockTestMapper;
import com.enterprise.kb.ielts.model.IeltsMockTest;
import com.enterprise.kb.ielts.model.IeltsStudyProfile;
import com.enterprise.kb.ielts.service.IeltsStudyProfileService;
import com.enterprise.kb.ielts.service.IeltsStudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/ielts/dashboard")
@RequiredArgsConstructor
public class IeltsDashboardController {

    private final IeltsStudyProfileService profileService;
    private final IeltsStudyService studyService;
    private final IeltsMistakeLogMapper mistakeLogMapper;
    private final IeltsMockTestMapper mockTestMapper;

    @GetMapping
    public ApiResponse<DashboardResponse> dashboard() {
        IeltsStudyProfile profile = profileService.getProfile();
        TodayPlanResponse todayPlan = studyService.getTodayPlan();
        StudyStatsResponse stats = studyService.getStats();
        List<MistakeStat> topMistakes = mistakeLogMapper.findStats(Instant.now().minus(30, ChronoUnit.DAYS), 5);
        IeltsMockTest latest = mockTestMapper.findLatest().orElse(null);
        String weakestSkill = inferWeakestSkill(profile, latest);
        String recommendation = todayPlan.completedItems() < todayPlan.totalItems()
                ? "先完成今日学习计划，再做专项训练"
                : "今日计划已完成，可补一组 " + weakestSkill + " 专项训练";
        return ApiResponse.ok(new DashboardResponse(profile, todayPlan, stats, weakestSkill, topMistakes, latest, recommendation));
    }

    private String inferWeakestSkill(IeltsStudyProfile profile, IeltsMockTest latest) {
        if (profile.getFocusSkills() != null && !profile.getFocusSkills().isBlank()) {
            return profile.getFocusSkills().split(",")[0].trim().toUpperCase();
        }
        if (latest != null && latest.getNextFocus() != null && !latest.getNextFocus().isBlank()) {
            return latest.getNextFocus();
        }
        return "READING";
    }
}
