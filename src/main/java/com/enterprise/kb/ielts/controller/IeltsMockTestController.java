package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.dto.MockTestTrendResponse;
import com.enterprise.kb.ielts.mapper.IeltsMockTestMapper;
import com.enterprise.kb.ielts.mapper.IeltsMockTestSectionMapper;
import com.enterprise.kb.ielts.model.IeltsMockTest;
import com.enterprise.kb.ielts.model.IeltsMockTestSection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/ielts/mock-tests")
@RequiredArgsConstructor
public class IeltsMockTestController {

    private final IeltsMockTestMapper mockTestMapper;
    private final IeltsMockTestSectionMapper sectionMapper;

    @GetMapping
    public ApiResponse<List<IeltsMockTest>> list(@RequestParam(defaultValue = "20") int limit) {
        List<IeltsMockTest> tests = mockTestMapper.findRecent(Math.min(Math.max(1, limit), 100));
        tests.forEach(test -> test.setSections(sectionMapper.findByMockTestId(test.getId())));
        return ApiResponse.ok(tests);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ApiResponse<IeltsMockTest> create(@RequestBody IeltsMockTest mockTest) {
        Instant now = Instant.now();
        mockTest.setId(UUID.randomUUID());
        mockTest.setCreatedAt(now);
        mockTest.setUpdatedAt(now);
        mockTestMapper.insert(mockTest);
        if (mockTest.getSections() != null) {
            for (IeltsMockTestSection section : mockTest.getSections()) {
                section.setId(UUID.randomUUID());
                section.setMockTestId(mockTest.getId());
                section.setCreatedAt(now);
                sectionMapper.insert(section);
            }
        }
        return ApiResponse.ok(mockTest, "模考记录已创建");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        mockTestMapper.deleteById(id);
        return ApiResponse.ok(null, "模考记录已删除");
    }

    @GetMapping("/trends")
    public ApiResponse<MockTestTrendResponse> trends() {
        List<IeltsMockTest> tests = mockTestMapper.findRecent(50);
        tests.sort(Comparator.comparing(IeltsMockTest::getTestDate));
        List<MockTestTrendResponse.Point> overall = tests.stream()
                .filter(t -> t.getOverallScore() != null)
                .map(t -> new MockTestTrendResponse.Point(t.getTestDate(), t.getOverallScore()))
                .toList();
        Map<String, List<MockTestTrendResponse.Point>> bySkill = new HashMap<>();
        for (IeltsMockTest test : tests) {
            for (IeltsMockTestSection section : sectionMapper.findByMockTestId(test.getId())) {
                if (section.getScore() != null) {
                    bySkill.computeIfAbsent(section.getSkill(), k -> new ArrayList<>())
                            .add(new MockTestTrendResponse.Point(test.getTestDate(), section.getScore()));
                }
            }
        }
        return ApiResponse.ok(new MockTestTrendResponse(
                overall,
                bySkill.getOrDefault("LISTENING", List.of()),
                bySkill.getOrDefault("READING", List.of()),
                bySkill.getOrDefault("WRITING", List.of()),
                bySkill.getOrDefault("SPEAKING", List.of())
        ));
    }
}
