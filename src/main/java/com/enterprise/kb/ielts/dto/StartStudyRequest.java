package com.enterprise.kb.ielts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * 开始学习某条内容的请求
 *
 * @param contentType 内容类型（WORD/PHRASE/PARAPHRASE 等）
 * @param contentId   内容 ID
 */
public record StartStudyRequest(
        @NotBlank
        @Pattern(
                regexp = "WORD|PHRASE|PARAPHRASE|PRONUNCIATION|GRAMMAR_POINT|GRAMMAR_EXERCISE|SPEAKING|LISTENING|READING|WRITING",
                message = "contentType 必须为有效的 IELTS 内容类型"
        )
        String contentType,
        @NotNull UUID contentId
) {}
