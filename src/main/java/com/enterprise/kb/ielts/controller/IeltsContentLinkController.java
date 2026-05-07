package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.dto.AddLinkRequest;
import com.enterprise.kb.ielts.dto.ContentLinkDto;
import com.enterprise.kb.ielts.model.IeltsContentLink;
import com.enterprise.kb.ielts.service.IeltsContentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 内容关联管理接口。
 * 路径约定：/api/ielts/{skill-resource}/{id}/links 处理技能内容的正向关联；
 *           /api/ielts/{cross-resource}/{id}/links 处理跨技能内容的反向引用。
 * 两类请求由不同路径前缀映射，统一委托给 IeltsContentLinkService。
 */
@RestController
@RequiredArgsConstructor
public class IeltsContentLinkController {

    private final IeltsContentLinkService linkService;

    // ── 技能内容正向关联（听力 / 阅读 / 写作 / 口语）──────────────────────

    @GetMapping("/api/ielts/listening-items/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getListeningLinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listBySource("LISTENING", id));
    }

    @PostMapping("/api/ielts/listening-items/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsContentLink> addListeningLink(@PathVariable UUID id,
                                                           @RequestBody AddLinkRequest req) {
        return ApiResponse.ok(linkService.addLink("LISTENING", id, req), "关联添加成功");
    }

    @DeleteMapping("/api/ielts/listening-items/{id}/links/{linkId}")
    public ApiResponse<Void> removeListeningLink(@PathVariable UUID id,
                                                  @PathVariable UUID linkId) {
        linkService.removeLink(linkId);
        return ApiResponse.ok(null, "关联删除成功");
    }

    @GetMapping("/api/ielts/reading-items/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getReadingLinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listBySource("READING", id));
    }

    @PostMapping("/api/ielts/reading-items/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsContentLink> addReadingLink(@PathVariable UUID id,
                                                         @RequestBody AddLinkRequest req) {
        return ApiResponse.ok(linkService.addLink("READING", id, req), "关联添加成功");
    }

    @DeleteMapping("/api/ielts/reading-items/{id}/links/{linkId}")
    public ApiResponse<Void> removeReadingLink(@PathVariable UUID id,
                                                @PathVariable UUID linkId) {
        linkService.removeLink(linkId);
        return ApiResponse.ok(null, "关联删除成功");
    }

    @GetMapping("/api/ielts/writing-tasks/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getWritingLinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listBySource("WRITING", id));
    }

    @PostMapping("/api/ielts/writing-tasks/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsContentLink> addWritingLink(@PathVariable UUID id,
                                                         @RequestBody AddLinkRequest req) {
        return ApiResponse.ok(linkService.addLink("WRITING", id, req), "关联添加成功");
    }

    @DeleteMapping("/api/ielts/writing-tasks/{id}/links/{linkId}")
    public ApiResponse<Void> removeWritingLink(@PathVariable UUID id,
                                                @PathVariable UUID linkId) {
        linkService.removeLink(linkId);
        return ApiResponse.ok(null, "关联删除成功");
    }

    @GetMapping("/api/ielts/speaking-topics/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getSpeakingLinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listBySource("SPEAKING", id));
    }

    @PostMapping("/api/ielts/speaking-topics/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsContentLink> addSpeakingLink(@PathVariable UUID id,
                                                          @RequestBody AddLinkRequest req) {
        return ApiResponse.ok(linkService.addLink("SPEAKING", id, req), "关联添加成功");
    }

    @DeleteMapping("/api/ielts/speaking-topics/{id}/links/{linkId}")
    public ApiResponse<Void> removeSpeakingLink(@PathVariable UUID id,
                                                  @PathVariable UUID linkId) {
        linkService.removeLink(linkId);
        return ApiResponse.ok(null, "关联删除成功");
    }

    // ── 跨技能内容反向引用（单词 / 短语 / 同义替换 / 发音 / 语法要点）──────

    @GetMapping("/api/ielts/words/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getWordBacklinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listByTarget("WORD", id));
    }

    @GetMapping("/api/ielts/phrases/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getPhraseBacklinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listByTarget("PHRASE", id));
    }

    @GetMapping("/api/ielts/paraphrase-groups/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getParaphraseBacklinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listByTarget("PARAPHRASE", id));
    }

    @GetMapping("/api/ielts/pronunciation-points/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getPronunciationBacklinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listByTarget("PRONUNCIATION", id));
    }

    @GetMapping("/api/ielts/grammar-points/{id}/links")
    public ApiResponse<List<ContentLinkDto>> getGrammarPointBacklinks(@PathVariable UUID id) {
        return ApiResponse.ok(linkService.listByTarget("GRAMMAR_POINT", id));
    }
}
