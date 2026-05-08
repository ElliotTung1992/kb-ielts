package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.mapper.IeltsWritingSubmissionMapper;
import com.enterprise.kb.ielts.model.IeltsWritingSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ielts/writing-submissions")
@RequiredArgsConstructor
public class IeltsWritingSubmissionController {

    private final IeltsWritingSubmissionMapper submissionMapper;

    @GetMapping
    public ApiResponse<List<IeltsWritingSubmission>> list(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(submissionMapper.findRecent(Math.min(Math.max(1, limit), 100)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsWritingSubmission> create(@RequestBody IeltsWritingSubmission submission) {
        Instant now = Instant.now();
        submission.setId(UUID.randomUUID());
        submission.setCreatedAt(now);
        submission.setUpdatedAt(now);
        submissionMapper.insert(submission);
        return ApiResponse.ok(submission, "作文记录已保存");
    }
}
