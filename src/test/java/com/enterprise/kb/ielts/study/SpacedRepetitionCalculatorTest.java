package com.enterprise.kb.ielts.study;

import com.enterprise.kb.ielts.model.IeltsStudyRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpacedRepetitionCalculatorTest {

    @Test
    void againResetsWordLearningAndKeepsEaseFactorAboveMinimum() {
        IeltsStudyRecord record = wordRecord(4, 10, "1.35");

        SpacedRepetitionCalculator.apply(record, "AGAIN");

        assertThat(record.getRepetitionCount()).isZero();
        assertThat(record.getIntervalDays()).isEqualTo(1);
        assertThat(record.getEaseFactor()).isEqualByComparingTo("1.30");
        assertThat(record.getStatus()).isEqualTo("LEARNING");
        assertThat(record.getNextReviewAt()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(record.getLastReviewedAt()).isNotNull();
    }

    @Test
    void goodPromotesWordToReviewingBeforeMasteryThreshold() {
        IeltsStudyRecord record = wordRecord(1, 1, "2.50");

        SpacedRepetitionCalculator.apply(record, "GOOD");

        assertThat(record.getRepetitionCount()).isEqualTo(2);
        assertThat(record.getIntervalDays()).isEqualTo(6);
        assertThat(record.getEaseFactor()).isEqualByComparingTo("2.50");
        assertThat(record.getStatus()).isEqualTo("REVIEWING");
    }

    @Test
    void easyCanMasterWordWhenIntervalThresholdIsReached() {
        IeltsStudyRecord record = wordRecord(2, 10, "2.50");

        SpacedRepetitionCalculator.apply(record, "EASY");

        assertThat(record.getRepetitionCount()).isEqualTo(3);
        assertThat(record.getIntervalDays()).isEqualTo(32);
        assertThat(record.getEaseFactor()).isEqualByComparingTo("2.65");
        assertThat(record.getStatus()).isEqualTo("MASTERED");
    }

    @Test
    void nonSm2ContentUsesSimpleMasteryRule() {
        IeltsStudyRecord record = baseRecord("READING", 0, 1, "2.50");

        SpacedRepetitionCalculator.apply(record, "GOOD");

        assertThat(record.getRepetitionCount()).isEqualTo(1);
        assertThat(record.getIntervalDays()).isEqualTo(365);
        assertThat(record.getStatus()).isEqualTo("MASTERED");
    }

    @Test
    void invalidRatingThrowsException() {
        IeltsStudyRecord record = wordRecord(0, 1, "2.50");

        assertThatThrownBy(() -> SpacedRepetitionCalculator.apply(record, "OK"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid rating");
    }

    private static IeltsStudyRecord wordRecord(int repetitionCount, int intervalDays, String easeFactor) {
        return baseRecord("WORD", repetitionCount, intervalDays, easeFactor);
    }

    private static IeltsStudyRecord baseRecord(String contentType, int repetitionCount, int intervalDays, String easeFactor) {
        IeltsStudyRecord record = new IeltsStudyRecord();
        record.setContentType(contentType);
        record.setRepetitionCount(repetitionCount);
        record.setIntervalDays(intervalDays);
        record.setEaseFactor(new BigDecimal(easeFactor));
        record.setStatus("LEARNING");
        return record;
    }
}
