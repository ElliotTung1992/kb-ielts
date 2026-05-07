package com.enterprise.kb.ielts.service.impl;

import com.enterprise.kb.common.exception.ResourceNotFoundException;
import com.enterprise.kb.ielts.config.IeltsStudyConfig;
import com.enterprise.kb.ielts.dto.ContentLinkDto;
import com.enterprise.kb.ielts.dto.ReviewRequest;
import com.enterprise.kb.ielts.dto.StudyPlanItem;
import com.enterprise.kb.ielts.dto.StudyStatsResponse;
import com.enterprise.kb.ielts.dto.TodayPlanResponse;
import com.enterprise.kb.ielts.mapper.*;
import com.enterprise.kb.ielts.model.IeltsDailyPlan;
import com.enterprise.kb.ielts.model.IeltsDailyPlanItem;
import com.enterprise.kb.ielts.model.IeltsReviewLog;
import com.enterprise.kb.ielts.model.IeltsStudyRecord;
import com.enterprise.kb.ielts.service.IeltsStudyService;
import com.enterprise.kb.ielts.study.SpacedRepetitionCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IeltsStudyServiceImpl implements IeltsStudyService {

    private final IeltsStudyRecordMapper recordMapper;
    private final IeltsReviewLogMapper reviewLogMapper;
    private final IeltsDailyPlanMapper dailyPlanMapper;
    private final IeltsDailyPlanItemMapper dailyPlanItemMapper;
    private final IeltsStudyConfig studyConfig;
    private final IeltsContentLinkMapper contentLinkMapper;

    private final IeltsWordMapper wordMapper;
    private final IeltsPhraseMapper phraseMapper;
    private final IeltsParaphraseGroupMapper paraphraseGroupMapper;
    private final IeltsPronunciationPointMapper pronunciationPointMapper;
    private final IeltsGrammarPointMapper grammarPointMapper;
    private final IeltsGrammarExerciseMapper grammarExerciseMapper;
    private final IeltsSpeakingTopicMapper speakingTopicMapper;
    private final IeltsListeningItemMapper listeningItemMapper;
    private final IeltsReadingItemMapper readingItemMapper;
    private final IeltsWritingTaskMapper writingTaskMapper;
    private final SpacedRepetitionCalculator repetitionCalculator;

    @Override
    @Transactional
    public TodayPlanResponse getTodayPlan() {
        LocalDate today = LocalDate.now();

        IeltsDailyPlan plan = dailyPlanMapper.findByPlanDate(today).orElse(null);
        if (plan != null && dailyPlanItemMapper.countByPlanId(plan.getId()) > 0) {
            List<StudyPlanItem> persistedItems = dailyPlanItemMapper.findItemsByPlanId(plan.getId());
            enrichLinkedItems(persistedItems);
            syncPlanCounts(plan);
            return new TodayPlanResponse(today, plan.getTotalItems(), plan.getCompletedItems(), persistedItems);
        }

        // 1. 获取所有到期复习项（单条 JOIN 查询，带摘要）
        List<StudyPlanItem> reviewItems = recordMapper.findDueItemsWithSummary(today);

        // 2. 统计各类型到期复习项数量，用于计算新学配额
        Map<String, Long> dueCounts = reviewItems.stream()
                .collect(Collectors.groupingBy(StudyPlanItem::getContentType, Collectors.counting()));

        // 3. 补充新学内容（各类型按每日配额 - 已到期数量 补足）
        List<StudyPlanItem> newItems = new ArrayList<>();
        int words       = studyConfig.getDailyWords();
        int phrases     = studyConfig.getDailyPhrases();
        int grammar     = studyConfig.getDailyGrammar();
        int others      = studyConfig.getDailyOthers();

        newItems.addAll(fetchNewItems("WORD",             words,   dueCounts,
                wordMapper::findNewContent,          w -> w.getId(), w -> w.getWord()));
        newItems.addAll(fetchNewItems("PHRASE",           phrases, dueCounts,
                phraseMapper::findNewContent,        p -> p.getId(), p -> p.getPhrase()));
        newItems.addAll(fetchNewItems("PARAPHRASE",       others,  dueCounts,
                paraphraseGroupMapper::findNewContent, pg -> pg.getId(), pg -> pg.getGroupName()));
        newItems.addAll(fetchNewItems("PRONUNCIATION",    others,  dueCounts,
                pronunciationPointMapper::findNewContent, pp -> pp.getId(), pp -> pp.getTitle()));
        newItems.addAll(fetchNewItems("GRAMMAR_POINT",    grammar, dueCounts,
                grammarPointMapper::findNewContent,  gp -> gp.getId(), gp -> gp.getTitle()));
        newItems.addAll(fetchNewItems("GRAMMAR_EXERCISE", grammar, dueCounts,
                grammarExerciseMapper::findNewContent, ge -> ge.getId(),
                ge -> ge.getQuestion() != null && ge.getQuestion().length() > 80
                        ? ge.getQuestion().substring(0, 80) : ge.getQuestion()));
        newItems.addAll(fetchNewItems("SPEAKING",         others,  dueCounts,
                speakingTopicMapper::findNewContent, st -> st.getId(), st -> st.getTitle()));
        newItems.addAll(fetchNewItems("LISTENING",        others,  dueCounts,
                listeningItemMapper::findNewContent, li -> li.getId(), li -> li.getTitle()));
        newItems.addAll(fetchNewItems("READING",          others,  dueCounts,
                readingItemMapper::findNewContent,   ri -> ri.getId(), ri -> ri.getTitle()));
        newItems.addAll(fetchNewItems("WRITING",          others,  dueCounts,
                writingTaskMapper::findNewContent,   wt -> wt.getId(), wt -> wt.getTitle()));

        // 4. 合并：复习项优先，新学项置后
        List<StudyPlanItem> allItems = new ArrayList<>(reviewItems);
        allItems.addAll(newItems);

        // 5. 为技能内容条目（听力/阅读/写作/口语）附带关联内容快照
        enrichLinkedItems(allItems);

        // 7. 创建或更新今日计划记录
        int total = allItems.size();
        boolean isNewPlan = false;
        if (plan == null) {
            isNewPlan = true;
            IeltsDailyPlan newPlan = new IeltsDailyPlan();
            newPlan.setId(UUID.randomUUID());
            newPlan.setPlanDate(today);
            newPlan.setCompletedItems(0);
            newPlan.setGeneratedAt(Instant.now());
            plan = newPlan;
        }
        plan.setTotalItems(total);
        plan.setCompletedItems(0);
        if (isNewPlan) {
            dailyPlanMapper.insert(plan);
        } else {
            dailyPlanMapper.update(plan);
        }
        savePlanItems(plan.getId(), allItems);

        return new TodayPlanResponse(today, plan.getTotalItems(), plan.getCompletedItems(), allItems);
    }

    /**
     * 从指定内容 Mapper 中取最多 (dailyQuota - 已到期数) 条新内容，转为 StudyPlanItem。
     */
    private <T> List<StudyPlanItem> fetchNewItems(
            String contentType, int dailyQuota,
            Map<String, Long> dueCounts,
            Function<Integer, List<T>> fetcher,
            Function<T, UUID> idExtractor,
            Function<T, String> summaryExtractor) {
        int due  = dueCounts.getOrDefault(contentType, 0L).intValue();
        int need = Math.max(0, dailyQuota - due);
        if (need == 0) return List.of();
        return fetcher.apply(need).stream()
                .map(item -> StudyPlanItem.forNew(
                        contentType, idExtractor.apply(item), summaryExtractor.apply(item)))
                .toList();
    }

    @Override
    @Transactional
    public IeltsStudyRecord startStudying(String contentType, UUID contentId) {
        return recordMapper.findByContentTypeAndContentId(contentType, contentId)
                .map(record -> {
                    attachRecordToTodayPlan(contentType, contentId, record.getId());
                    return record;
                })
                .orElseGet(() -> {
                    IeltsStudyRecord record = new IeltsStudyRecord();
                    record.setId(UUID.randomUUID());
                    record.setContentType(contentType);
                    record.setContentId(contentId);
                    record.setStatus("LEARNING");
                    record.setEaseFactor(new BigDecimal("2.50"));
                    record.setIntervalDays(1);
                    record.setRepetitionCount(0);
                    record.setNextReviewAt(LocalDate.now());
                    record.setLastReviewedAt(Instant.now());
                    record.setCreatedAt(Instant.now());
                    recordMapper.insert(record);
                    attachRecordToTodayPlan(contentType, contentId, record.getId());
                    log.debug("新建学习记录: contentType={}, contentId={}", contentType, contentId);
                    return record;
                });
    }

    @Override
    @Transactional
    public IeltsStudyRecord submitReview(ReviewRequest request) {
        IeltsStudyRecord record = recordMapper.findById(request.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("IeltsStudyRecord", request.recordId()));

        ReviewSnapshot before = ReviewSnapshot.from(record);

        // 应用 SM-2 或简单切换
        repetitionCalculator.applyWithClock(record, request.rating());
        recordMapper.update(record);

        ReviewSnapshot after = ReviewSnapshot.from(record);

        // 记录复习日志
        IeltsReviewLog reviewLog = new IeltsReviewLog();
        reviewLog.setId(UUID.randomUUID());
        reviewLog.setRecordId(record.getId());
        reviewLog.setRating(request.rating());
        reviewLog.setReviewedAt(Instant.now());
        before.applyBefore(reviewLog);
        after.applyAfter(reviewLog);
        reviewLogMapper.insert(reviewLog);

        // 更新今日计划完成数
        updateTodayCompleted(record);

        return record;
    }

    @Override
    @Transactional(readOnly = true)
    public StudyStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        long total = recordMapper.countAll();
        long learning = recordMapper.countByStatus("LEARNING");
        long reviewing = recordMapper.countByStatus("REVIEWING");
        long mastered = recordMapper.countByStatus("MASTERED");
        long todayReviews = reviewLogMapper.countByDate(today);
        int streak = computeStreak();
        return new StudyStatsResponse(total, learning, reviewing, mastered, todayReviews, streak);
    }

    private void updateTodayCompleted(IeltsStudyRecord record) {
        LocalDate today = LocalDate.now();
        dailyPlanMapper.findByPlanDate(today).ifPresent(plan -> {
            Instant completedAt = Instant.now();
            int updated = dailyPlanItemMapper.markCompletedByRecordId(plan.getId(), record.getId(), completedAt);
            if (updated == 0) {
                dailyPlanItemMapper.markCompletedByContent(
                        plan.getId(), record.getContentType(), record.getContentId(), record.getId(), completedAt);
            }
            syncPlanCounts(plan);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyPlanItem> getRecordsByStatus(String status) {
        return recordMapper.findByStatusWithSummary(status);
    }

    private int computeStreak() {
        List<IeltsDailyPlan> recent = dailyPlanMapper.findRecent(365);
        int streak = 0;
        LocalDate expected = LocalDate.now();
        for (IeltsDailyPlan plan : recent) {
            if (plan.getCompletedItems() > 0 && plan.getPlanDate().equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private void enrichLinkedItems(List<StudyPlanItem> items) {
        java.util.Set<String> skillTypes = java.util.Set.of("LISTENING", "READING", "WRITING", "SPEAKING");
        items.stream()
                .filter(item -> skillTypes.contains(item.getContentType()))
                .forEach(item -> {
                    List<ContentLinkDto> links = contentLinkMapper.findBySource(
                            item.getContentType(), item.getContentId());
                    item.setLinkedItems(links);
                });
    }

    private void savePlanItems(UUID planId, List<StudyPlanItem> items) {
        if (items.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        List<IeltsDailyPlanItem> planItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            StudyPlanItem item = items.get(i);
            IeltsDailyPlanItem planItem = new IeltsDailyPlanItem();
            planItem.setId(UUID.randomUUID());
            planItem.setPlanId(planId);
            planItem.setContentType(item.getContentType());
            planItem.setContentId(item.getContentId());
            planItem.setRecordId(item.getRecordId());
            planItem.setStudyMode(item.getStudyMode());
            planItem.setStatus("PENDING");
            planItem.setSummary(item.getSummary());
            planItem.setSortOrder(i);
            planItem.setCreatedAt(now);
            planItems.add(planItem);
        }
        dailyPlanItemMapper.batchInsert(planItems);
    }

    private void attachRecordToTodayPlan(String contentType, UUID contentId, UUID recordId) {
        dailyPlanMapper.findByPlanDate(LocalDate.now())
                .ifPresent(plan -> dailyPlanItemMapper.updateRecordIdByContent(
                        plan.getId(), contentType, contentId, recordId));
    }

    private void syncPlanCounts(IeltsDailyPlan plan) {
        plan.setTotalItems(dailyPlanItemMapper.countByPlanId(plan.getId()));
        plan.setCompletedItems(dailyPlanItemMapper.countCompletedByPlanId(plan.getId()));
        dailyPlanMapper.update(plan);
    }

    private record ReviewSnapshot(
            String status,
            Integer intervalDays,
            Integer repetitionCount,
            java.math.BigDecimal easeFactor
    ) {

        static ReviewSnapshot from(IeltsStudyRecord record) {
            return new ReviewSnapshot(
                    record.getStatus(),
                    record.getIntervalDays(),
                    record.getRepetitionCount(),
                    record.getEaseFactor()
            );
        }

        void applyBefore(IeltsReviewLog log) {
            log.setBeforeStatus(status);
            log.setBeforeIntervalDays(intervalDays);
            log.setBeforeRepetitionCount(repetitionCount);
            log.setBeforeEaseFactor(easeFactor);
        }

        void applyAfter(IeltsReviewLog log) {
            log.setAfterStatus(status);
            log.setAfterIntervalDays(intervalDays);
            log.setAfterRepetitionCount(repetitionCount);
            log.setAfterEaseFactor(easeFactor);
        }
    }
}
