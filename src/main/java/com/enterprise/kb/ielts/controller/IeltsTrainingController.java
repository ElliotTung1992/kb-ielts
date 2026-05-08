package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.common.dto.PageResponse;
import com.enterprise.kb.ielts.model.*;
import com.enterprise.kb.ielts.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ielts/training")
@RequiredArgsConstructor
public class IeltsTrainingController {

    private final IeltsListeningItemService listeningItemService;
    private final IeltsReadingItemService readingItemService;
    private final IeltsWritingTaskService writingTaskService;
    private final IeltsSpeakingTopicService speakingTopicService;

    @GetMapping("/listening")
    public ApiResponse<PageResponse<IeltsListeningItem>> listening(@RequestParam(required = false) Integer section,
                                                                   @RequestParam(required = false) String questionType,
                                                                   @RequestParam(required = false) Integer difficulty,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(listeningItemService.listItems(difficulty, section, questionType, null, null, page, size));
    }

    @GetMapping("/reading")
    public ApiResponse<PageResponse<IeltsReadingItem>> reading(@RequestParam(required = false) String questionType,
                                                               @RequestParam(required = false) Integer difficulty,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(readingItemService.listItems(difficulty, null, questionType, null, null, page, size));
    }

    @GetMapping("/writing")
    public ApiResponse<PageResponse<IeltsWritingTask>> writing(@RequestParam(required = false) Integer taskNumber,
                                                               @RequestParam(required = false) Integer difficulty,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(writingTaskService.listTasks(difficulty, taskNumber, null, null, null, page, size));
    }

    @GetMapping("/speaking")
    public ApiResponse<PageResponse<IeltsSpeakingTopic>> speaking(@RequestParam(required = false) Integer part,
                                                                  @RequestParam(required = false) Integer difficulty,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(speakingTopicService.listTopics(difficulty, part, null, null, page, size));
    }
}
