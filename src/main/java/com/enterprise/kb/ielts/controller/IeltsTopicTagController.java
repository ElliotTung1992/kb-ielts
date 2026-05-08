package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.common.exception.ResourceNotFoundException;
import com.enterprise.kb.ielts.mapper.IeltsTopicTagMapper;
import com.enterprise.kb.ielts.model.IeltsTopicTag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ielts/topic-tags")
@RequiredArgsConstructor
public class IeltsTopicTagController {

    private final IeltsTopicTagMapper tagMapper;

    @GetMapping
    public ApiResponse<List<IeltsTopicTag>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String category,
                                                 @RequestParam(required = false) String skill,
                                                 @RequestParam(required = false) Boolean enabled,
                                                 @RequestParam(defaultValue = "100") int limit) {
        int safeLimit = Math.min(Math.max(1, limit), 500);
        return ApiResponse.ok(tagMapper.findAll(keyword, category, skill, enabled, safeLimit));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsTopicTag> create(@RequestBody IeltsTopicTag tag) {
        normalize(tag);
        Instant now = Instant.now();
        tag.setId(UUID.randomUUID());
        tag.setUsageCount(tag.getUsageCount() == null ? 0 : tag.getUsageCount());
        tag.setEnabled(tag.getEnabled() == null || tag.getEnabled());
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
        try {
            tagMapper.insert(tag);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("标签已存在");
        }
        return ApiResponse.ok(tag, "话题标签已创建");
    }

    @PutMapping("/{id}")
    public ApiResponse<IeltsTopicTag> update(@PathVariable UUID id, @RequestBody IeltsTopicTag tag) {
        IeltsTopicTag existing = tagMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IeltsTopicTag", id));
        normalize(tag);
        tag.setId(id);
        tag.setUsageCount(tag.getUsageCount() == null ? existing.getUsageCount() : tag.getUsageCount());
        tag.setEnabled(tag.getEnabled() == null ? existing.getEnabled() : tag.getEnabled());
        tag.setCreatedAt(existing.getCreatedAt());
        tag.setUpdatedAt(Instant.now());
        try {
            tagMapper.update(tag);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("标签已存在");
        }
        return ApiResponse.ok(tag, "话题标签已更新");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        tagMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IeltsTopicTag", id));
        tagMapper.deleteById(id);
        return ApiResponse.ok(null, "话题标签已删除");
    }

    private void normalize(IeltsTopicTag tag) {
        if (tag.getTagName() == null || tag.getTagName().trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        tag.setTagName(tag.getTagName().trim());
        tag.setCategory(blankToNull(tag.getCategory()));
        tag.setSkillTags(blankToNull(tag.getSkillTags()));
        tag.setDescription(blankToNull(tag.getDescription()));
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
