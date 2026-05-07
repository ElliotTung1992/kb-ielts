package com.enterprise.kb.ielts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddLinkRequest(
        @NotBlank
        @Pattern(
                regexp = "WORD|PHRASE|PARAPHRASE|PRONUNCIATION|GRAMMAR_POINT",
                message = "targetType 必须为 WORD/PHRASE/PARAPHRASE/PRONUNCIATION/GRAMMAR_POINT"
        )
        String targetType,
        @NotNull UUID targetId,
        @Size(max = 500, message = "note 不能超过 500 个字符") String note
) {}
