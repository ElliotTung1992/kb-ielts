package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.common.dto.PageResponse;
import com.enterprise.kb.ielts.model.IeltsGrammarPoint;
import com.enterprise.kb.ielts.service.IeltsGrammarPointService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 雅思语法要点管理接口
 */
@RestController
@RequestMapping("/api/ielts/grammar-points")
@RequiredArgsConstructor
@Validated
public class IeltsGrammarPointController {

    private final IeltsGrammarPointService pointService;

    /**
     * 分页查询语法要点列表
     *
     * @param difficulty 难度筛选
     * @param category   分类筛选
     * @param topicTags  话题标签
     * @param page       页码
     * @param size       每页条数
     * @return 分页语法要点列表
     */
    @GetMapping
    public ApiResponse<PageResponse<IeltsGrammarPoint>> list(
            @RequestParam(required = false) @Min(value = 1, message = "difficulty 必须在 1-3 之间") @Max(value = 3, message = "difficulty 必须在 1-3 之间") Integer difficulty,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String topicTags,
            @RequestParam(required = false) @Pattern(regexp = "NEW|LEARNING|REVIEWING|MASTERED", message = "studyStatus 必须为 NEW/LEARNING/REVIEWING/MASTERED") String studyStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 必须大于等于 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size 必须在 1-100 之间") @Max(value = 100, message = "size 必须在 1-100 之间") int size) {
        return ApiResponse.ok(pointService.listPoints(difficulty, category, topicTags, studyStatus, keyword, page, size));
    }

    /**
     * 查询语法要点详情
     *
     * @param id 要点 ID
     * @return 语法要点详情
     */
    @GetMapping("/{id}")
    public ApiResponse<IeltsGrammarPoint> getById(@PathVariable UUID id) {
        return ApiResponse.ok(pointService.getById(id));
    }

    /**
     * 新增语法要点
     *
     * @param point 语法要点信息
     * @return 创建后的语法要点
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsGrammarPoint> create(@RequestBody IeltsGrammarPoint point) {
        return ApiResponse.ok(pointService.create(point), "语法要点创建成功");
    }

    /**
     * 更新语法要点
     *
     * @param id    要点 ID
     * @param point 更新信息
     * @return 更新后的语法要点
     */
    @PutMapping("/{id}")
    public ApiResponse<IeltsGrammarPoint> update(@PathVariable UUID id, @RequestBody IeltsGrammarPoint point) {
        return ApiResponse.ok(pointService.update(id, point), "语法要点更新成功");
    }

    /**
     * 删除语法要点
     *
     * @param id 要点 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        pointService.delete(id);
        return ApiResponse.ok(null, "语法要点删除成功");
    }

    /**
     * 批量导入语法要点
     *
     * @param points 语法要点列表
     * @return 导入数量
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Integer>> batchImport(@RequestBody List<IeltsGrammarPoint> points) {
        int count = pointService.batchImport(points);
        return ApiResponse.ok(Map.of("imported", count), "批量导入完成");
    }
}
