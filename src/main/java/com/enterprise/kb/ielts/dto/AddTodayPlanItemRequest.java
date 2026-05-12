package com.enterprise.kb.ielts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AddTodayPlanItemRequest(
        @NotBlank
        @Pattern(regexp = ContentTypeConstants.CONTENT_TYPE_PATTERN, message = "contentType 必须为有效的 IELTS 内容类型")
        String contentType,
        @NotNull UUID contentId,
        String summary
) {}
