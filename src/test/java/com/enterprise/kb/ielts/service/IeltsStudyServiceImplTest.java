package com.enterprise.kb.ielts.service;

import com.enterprise.kb.ielts.config.IeltsStudyConfig;
import com.enterprise.kb.ielts.dto.StudyPlanItem;
import com.enterprise.kb.ielts.dto.TodayPlanResponse;
import com.enterprise.kb.ielts.mapper.*;
import com.enterprise.kb.ielts.model.IeltsWord;
import com.enterprise.kb.ielts.service.impl.IeltsStudyServiceImpl;
import com.enterprise.kb.ielts.study.SpacedRepetitionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IeltsStudyServiceImplTest {

    @Mock private IeltsStudyRecordMapper recordMapper;
    @Mock private IeltsReviewLogMapper reviewLogMapper;
    @Mock private IeltsDailyPlanMapper dailyPlanMapper;
    @Mock private IeltsDailyPlanItemMapper dailyPlanItemMapper;
    @Mock private IeltsStudyConfig studyConfig;
    @Mock private IeltsContentLinkMapper contentLinkMapper;
    @Mock private IeltsMistakeLogMapper mistakeLogMapper;
    @Mock private IeltsWordMapper wordMapper;
    @Mock private IeltsPhraseMapper phraseMapper;
    @Mock private IeltsParaphraseGroupMapper paraphraseGroupMapper;
    @Mock private IeltsPronunciationPointMapper pronunciationPointMapper;
    @Mock private IeltsGrammarPointMapper grammarPointMapper;
    @Mock private IeltsGrammarExerciseMapper grammarExerciseMapper;
    @Mock private IeltsSpeakingTopicMapper speakingTopicMapper;
    @Mock private IeltsListeningItemMapper listeningItemMapper;
    @Mock private IeltsReadingItemMapper readingItemMapper;
    @Mock private IeltsWritingTaskMapper writingTaskMapper;
    @Mock private SpacedRepetitionCalculator repetitionCalculator;

    @InjectMocks
    private IeltsStudyServiceImpl service;

    @BeforeEach
    void setUp() {
        when(dailyPlanMapper.findByPlanDate(any())).thenReturn(Optional.empty());
        when(studyConfig.getDailyWords()).thenReturn(0);
        when(studyConfig.getDailyPhrases()).thenReturn(0);
        when(studyConfig.getDailyOthers()).thenReturn(0);
    }

    @Test
    void todayPlan_reviewItemsLeadNewItems() {
        StudyPlanItem reviewWord = StudyPlanItem.forReview(UUID.randomUUID(), "WORD", UUID.randomUUID(), "abundant");
        when(recordMapper.findDueItemsWithSummary(any())).thenReturn(List.of(reviewWord));

        IeltsWord newWord = new IeltsWord();
        newWord.setId(UUID.randomUUID());
        newWord.setWord("allocate");
        when(studyConfig.getDailyWords()).thenReturn(2);
        when(wordMapper.findNewContent(1)).thenReturn(List.of(newWord));

        TodayPlanResponse response = service.getTodayPlan();

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).getStudyMode()).isEqualTo("REVIEW");
        assertThat(response.items().get(1).getStudyMode()).isEqualTo("NEW");
    }

    @Test
    void todayPlan_newItemsCapedAtQuotaMinusDue() {
        StudyPlanItem dueWord = StudyPlanItem.forReview(UUID.randomUUID(), "WORD", UUID.randomUUID(), "abundant");
        when(recordMapper.findDueItemsWithSummary(any())).thenReturn(List.of(dueWord));
        when(studyConfig.getDailyWords()).thenReturn(1);

        TodayPlanResponse response = service.getTodayPlan();

        // quota=1, due=1 → need=0, wordMapper never called, only the review item
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).getStudyMode()).isEqualTo("REVIEW");
    }

    @Test
    void todayPlan_returnsEmptyWhenNoItemsDue() {
        when(recordMapper.findDueItemsWithSummary(any())).thenReturn(List.of());

        TodayPlanResponse response = service.getTodayPlan();

        assertThat(response.items()).isEmpty();
    }
}
