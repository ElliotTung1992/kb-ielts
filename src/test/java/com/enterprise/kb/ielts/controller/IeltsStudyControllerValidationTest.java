package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.ielts.dto.ReviewRequest;
import com.enterprise.kb.ielts.dto.StudyPlanItem;
import com.enterprise.kb.ielts.dto.StudyStatsResponse;
import com.enterprise.kb.ielts.dto.TodayPlanResponse;
import com.enterprise.kb.ielts.exception.IeltsExceptionHandler;
import com.enterprise.kb.ielts.model.IeltsStudyRecord;
import com.enterprise.kb.ielts.service.IeltsStudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IeltsStudyControllerValidationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new IeltsStudyController(new NoopIeltsStudyService()))
                .setControllerAdvice(new IeltsExceptionHandler())
                .build();
    }

    @Test
    void reviewRejectsInvalidRating() throws Exception {
        String body = """
                {
                  "recordId": "%s",
                  "rating": "OK"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/ielts/study/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.message").value("rating 必须为 AGAIN/HARD/GOOD/EASY"));
    }

    @Test
    void startRejectsInvalidContentType() throws Exception {
        String body = """
                {
                  "contentType": "BOOK",
                  "contentId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/ielts/study/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.message").value("contentType 必须为有效的 IELTS 内容类型"));
    }

    static class NoopIeltsStudyService implements IeltsStudyService {

        @Override
        public TodayPlanResponse getTodayPlan() {
            return null;
        }

        @Override
        public IeltsStudyRecord startStudying(String contentType, UUID contentId) {
            return null;
        }

        @Override
        public IeltsStudyRecord submitReview(ReviewRequest request) {
            return null;
        }

        @Override
        public StudyStatsResponse getStats() {
            return null;
        }

        @Override
        public List<StudyPlanItem> getRecordsByStatus(String status) {
            return List.of();
        }
    }
}
