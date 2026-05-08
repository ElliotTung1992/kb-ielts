package com.enterprise.kb.ielts.service.impl;

import com.enterprise.kb.ielts.dto.PlanSuggestionResponse;
import com.enterprise.kb.ielts.mapper.IeltsStudyProfileMapper;
import com.enterprise.kb.ielts.model.IeltsStudyProfile;
import com.enterprise.kb.ielts.service.IeltsStudyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IeltsStudyProfileServiceImpl implements IeltsStudyProfileService {

    private final IeltsStudyProfileMapper profileMapper;

    @Override
    @Transactional
    public IeltsStudyProfile getProfile() {
        return profileMapper.findOne().orElseGet(this::createDefaultProfile);
    }

    @Override
    @Transactional
    public IeltsStudyProfile saveProfile(IeltsStudyProfile profile) {
        validateScore(profile.getTargetOverallScore());
        validateScore(profile.getTargetListeningScore());
        validateScore(profile.getTargetReadingScore());
        validateScore(profile.getTargetWritingScore());
        validateScore(profile.getTargetSpeakingScore());
        validateScore(profile.getCurrentOverallScore());
        if (profile.getExamDate() != null && profile.getExamDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("examDate 不能早于今天");
        }
        if (profile.getDailyMinutes() == null || profile.getDailyMinutes() < 10 || profile.getDailyMinutes() > 240) {
            throw new IllegalArgumentException("dailyMinutes 必须在 10-240 之间");
        }
        IeltsStudyProfile existing = getProfile();
        profile.setId(existing.getId());
        profile.setCreatedAt(existing.getCreatedAt());
        profile.setUpdatedAt(Instant.now());
        profileMapper.update(profile);
        return profile;
    }

    @Override
    @Transactional
    public PlanSuggestionResponse getPlanSuggestion() {
        IeltsStudyProfile profile = getProfile();
        int days = profile.getExamDate() == null ? -1 : (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), profile.getExamDate()));
        String focus = profile.getFocusSkills() == null || profile.getFocusSkills().isBlank()
                ? "READING" : profile.getFocusSkills().split(",")[0].trim().toUpperCase();
        int minutes = profile.getDailyMinutes() == null ? 60 : profile.getDailyMinutes();
        int factor = Math.max(1, minutes / 30);
        int words = Math.min(60, 10 * factor);
        int phrases = Math.min(30, 5 * factor);
        int reading = "READING".equals(focus) ? 3 : 1;
        int writing = "WRITING".equals(focus) ? 2 : 1;
        String message = days >= 0 ? "距离考试还有 " + days + " 天，建议优先训练 " + focus : "未设置考试日期，按每日时长生成基础建议";
        return new PlanSuggestionResponse(days, focus, words, phrases, reading, writing, message);
    }

    private IeltsStudyProfile createDefaultProfile() {
        IeltsStudyProfile profile = new IeltsStudyProfile();
        Instant now = Instant.now();
        profile.setId(UUID.randomUUID());
        profile.setTargetOverallScore(new BigDecimal("7.0"));
        profile.setDailyMinutes(60);
        profile.setTrainingType("ACADEMIC");
        profile.setAccentPreference("UK");
        profile.setFocusSkills("READING");
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profileMapper.insert(profile);
        return profile;
    }

    private void validateScore(BigDecimal score) {
        if (score == null) return;
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("9.0")) > 0) {
            throw new IllegalArgumentException("雅思分数必须在 0-9 之间");
        }
        BigDecimal doubled = score.multiply(new BigDecimal("2"));
        if (doubled.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("雅思分数只支持 0.5 步进");
        }
    }
}
