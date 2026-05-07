package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.common.dto.PageResponse;
import com.enterprise.kb.ielts.model.IeltsWritingTask;
import com.enterprise.kb.ielts.service.IeltsWritingTaskService;
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
 * 雅思写作任务管理接口
 */
@RestController
@RequestMapping("/api/ielts/writing-tasks")
@RequiredArgsConstructor
@Validated
public class IeltsWritingTaskController {

    private final IeltsWritingTaskService taskService;

    /**
     * 分页查询写作任务列表
     *
     * @param difficulty   难度筛选
     * @param taskNumber   Task 编号（1或2）
     * @param trainingType 考试类型（ACADEMIC/GENERAL）
     * @param topicTags    话题标签
     * @param page         页码
     * @param size         每页条数
     * @return 分页写作任务列表
     */
    @GetMapping
    public ApiResponse<PageResponse<IeltsWritingTask>> list(
            @RequestParam(required = false) @Min(value = 1, message = "difficulty 必须在 1-3 之间") @Max(value = 3, message = "difficulty 必须在 1-3 之间") Integer difficulty,
            @RequestParam(required = false) @Min(value = 1, message = "taskNumber 必须为 1 或 2") @Max(value = 2, message = "taskNumber 必须为 1 或 2") Integer taskNumber,
            @RequestParam(required = false) @Pattern(regexp = "ACADEMIC|GENERAL", message = "trainingType 必须为 ACADEMIC/GENERAL") String trainingType,
            @RequestParam(required = false) String topicTags,
            @RequestParam(required = false) @Pattern(regexp = "NEW|LEARNING|REVIEWING|MASTERED", message = "studyStatus 必须为 NEW/LEARNING/REVIEWING/MASTERED") String studyStatus,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 必须大于等于 1") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size 必须在 1-100 之间") @Max(value = 100, message = "size 必须在 1-100 之间") int size) {
        return ApiResponse.ok(taskService.listTasks(difficulty, taskNumber, trainingType, topicTags, studyStatus, page, size));
    }

    /**
     * 查询写作任务详情
     *
     * @param id 任务 ID
     * @return 写作任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<IeltsWritingTask> getById(@PathVariable UUID id) {
        return ApiResponse.ok(taskService.getById(id));
    }

    /**
     * 新增写作任务
     *
     * @param task 任务信息
     * @return 创建后的任务
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsWritingTask> create(@RequestBody IeltsWritingTask task) {
        return ApiResponse.ok(taskService.create(task), "写作任务创建成功");
    }

    /**
     * 更新写作任务
     *
     * @param id   任务 ID
     * @param task 更新信息
     * @return 更新后的任务
     */
    @PutMapping("/{id}")
    public ApiResponse<IeltsWritingTask> update(@PathVariable UUID id, @RequestBody IeltsWritingTask task) {
        return ApiResponse.ok(taskService.update(id, task), "写作任务更新成功");
    }

    /**
     * 删除写作任务
     *
     * @param id 任务 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ApiResponse.ok(null, "写作任务删除成功");
    }

    /**
     * 批量导入写作任务
     *
     * @param tasks 任务列表
     * @return 导入数量
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Integer>> batchImport(@RequestBody List<IeltsWritingTask> tasks) {
        int count = taskService.batchImport(tasks);
        return ApiResponse.ok(Map.of("imported", count), "批量导入完成");
    }
}
