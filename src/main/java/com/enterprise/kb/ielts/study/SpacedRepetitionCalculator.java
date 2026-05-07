package com.enterprise.kb.ielts.study;

import com.enterprise.kb.ielts.model.IeltsStudyRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * SM-2 间隔重复算法计算器。
 * WORD / PHRASE 使用完整 SM-2；其余内容类型使用简化的 LEARNING/MASTERED 切换。
 */
@Component
public class SpacedRepetitionCalculator {

    private static final BigDecimal MIN_EASE_FACTOR = new BigDecimal("1.30");
    private static final Set<String> SM2_TYPES = Set.of("WORD", "PHRASE");
    private final Clock clock;

    public SpacedRepetitionCalculator(Clock clock) {
        this.clock = clock;
    }

    /**
     * 根据评分更新学习记录的 SM-2 参数
     *
     * @param record 当前学习记录
     * @param rating 评分：AGAIN / HARD / GOOD / EASY
     */
    public static void apply(IeltsStudyRecord record, String rating) {
        apply(record, rating, Clock.systemDefaultZone());
    }

    public void applyWithClock(IeltsStudyRecord record, String rating) {
        apply(record, rating, clock);
    }

    private static void apply(IeltsStudyRecord record, String rating, Clock clock) {
        if (SM2_TYPES.contains(record.getContentType())) {
            applySm2(record, rating);
        } else {
            applySimple(record, rating);
        }
        record.setLastReviewedAt(Instant.now(clock));
        record.setNextReviewAt(LocalDate.now(clock).plusDays(record.getIntervalDays()));
    }

    private static void applySm2(IeltsStudyRecord record, String rating) {
        int rep = record.getRepetitionCount();
        BigDecimal ef = record.getEaseFactor();
        int interval = record.getIntervalDays();

        switch (rating) {
            case "AGAIN" -> {
                record.setRepetitionCount(0);
                record.setIntervalDays(1);
                record.setEaseFactor(ef.subtract(new BigDecimal("0.20")).max(MIN_EASE_FACTOR));
                record.setStatus("LEARNING");
            }
            case "HARD" -> {
                int newRep = rep + 1;
                record.setRepetitionCount(newRep);
                record.setIntervalDays(Math.max(1, (int) Math.floor(interval * 1.2)));
                record.setEaseFactor(ef.subtract(new BigDecimal("0.15")).max(MIN_EASE_FACTOR));
                record.setStatus(newRep >= 2 ? "REVIEWING" : "LEARNING");
            }
            case "GOOD" -> {
                int newRep = rep + 1;
                int newInterval = computeInterval(newRep, interval, ef, 1.0);
                record.setRepetitionCount(newRep);
                record.setIntervalDays(newInterval);
                record.setStatus(newRep >= 5 && newInterval >= 21 ? "MASTERED" : "REVIEWING");
            }
            case "EASY" -> {
                int newRep = rep + 1;
                BigDecimal newEf = ef.add(new BigDecimal("0.15"));
                int newInterval = computeInterval(newRep, interval, ef, 1.3);
                record.setRepetitionCount(newRep);
                record.setIntervalDays(newInterval);
                record.setEaseFactor(newEf);
                record.setStatus(newRep >= 3 && newInterval >= 21 ? "MASTERED" : "REVIEWING");
            }
            default -> throw new IllegalArgumentException("Invalid rating: " + rating);
        }
    }

    private static int computeInterval(int rep, int prevInterval, BigDecimal ef, double bonus) {
        return switch (rep) {
            case 1 -> bonus > 1.0 ? 4 : 1;
            case 2 -> bonus > 1.0 ? 10 : 6;
            default -> Math.max(1, (int) Math.floor(prevInterval * ef.doubleValue() * bonus));
        };
    }

    private static void applySimple(IeltsStudyRecord record, String rating) {
        int newRep = record.getRepetitionCount() + 1;
        record.setRepetitionCount(newRep);
        switch (rating) {
            case "AGAIN" -> {
                record.setStatus("LEARNING");
                record.setIntervalDays(1);
            }
            case "HARD" -> {
                record.setStatus("LEARNING");
                record.setIntervalDays(Math.max(1, Math.min(3, record.getIntervalDays())));
            }
            case "GOOD" -> {
                record.setIntervalDays(simpleGoodInterval(newRep, record.getIntervalDays()));
                record.setStatus(newRep >= 4 && record.getIntervalDays() >= 30 ? "MASTERED" : "REVIEWING");
            }
            case "EASY" -> {
                record.setIntervalDays(simpleEasyInterval(newRep, record.getIntervalDays()));
                record.setStatus(newRep >= 3 && record.getIntervalDays() >= 30 ? "MASTERED" : "REVIEWING");
            }
            default -> throw new IllegalArgumentException("Invalid rating: " + rating);
        }
    }

    private static int simpleGoodInterval(int rep, int prevInterval) {
        return switch (rep) {
            case 1 -> 3;
            case 2 -> 7;
            case 3 -> 14;
            default -> Math.max(30, prevInterval * 2);
        };
    }

    private static int simpleEasyInterval(int rep, int prevInterval) {
        return switch (rep) {
            case 1 -> 7;
            case 2 -> 21;
            default -> Math.max(45, prevInterval * 2);
        };
    }
}
