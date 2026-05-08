package com.enterprise.kb.ielts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;
import java.util.List;

/**
 * 提交复习评分请求
 */
public record ReviewRequest(
        /** 学习记录 ID */
        @NotNull UUID recordId,
        /** 评分（AGAIN / HARD / GOOD / EASY） */
        @NotBlank @Pattern(regexp = "AGAIN|HARD|GOOD|EASY", message = "rating 必须为 AGAIN/HARD/GOOD/EASY") String rating,
        /** 错因类型列表；通常在 AGAIN / HARD 时提交 */
        List<@Pattern(regexp = "VOCABULARY|PARAPHRASE|GRAMMAR|PRONUNCIATION|QUESTION_READING|LOCATION|TIME|SPELLING|EXPRESSION", message = "mistakeType 不合法") String> mistakeTypes,
        /** 错因或复习备注 */
        String note
) {}
