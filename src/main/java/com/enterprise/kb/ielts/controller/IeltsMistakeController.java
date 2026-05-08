package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.dto.MistakeStat;
import com.enterprise.kb.ielts.mapper.IeltsMistakeLogMapper;
import com.enterprise.kb.ielts.model.IeltsMistakeLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ielts/mistakes")
@RequiredArgsConstructor
public class IeltsMistakeController {

    private final IeltsMistakeLogMapper mistakeLogMapper;

    @GetMapping("/stats")
    public ApiResponse<List<MistakeStat>> stats(@RequestParam(defaultValue = "30") int days,
                                                @RequestParam(defaultValue = "10") int limit) {
        Instant since = Instant.now().minus(Math.max(1, days), ChronoUnit.DAYS);
        return ApiResponse.ok(mistakeLogMapper.findStats(since, Math.min(Math.max(1, limit), 50)));
    }

    @GetMapping("/recent")
    public ApiResponse<List<IeltsMistakeLog>> recent(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(mistakeLogMapper.findRecent(Math.min(Math.max(1, limit), 100)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        mistakeLogMapper.deleteById(id);
        return ApiResponse.ok(null, "错因记录已删除");
    }
}
