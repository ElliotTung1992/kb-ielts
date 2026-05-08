package com.enterprise.kb.ielts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * 新增内容关联请求。
 */
public record AddLinkRequest(
        /** 目标跨技能内容类型：WORD / PHRASE / PARAPHRASE / PRONUNCIATION / GRAMMAR_POINT */
        @NotBlank
        @Pattern(
                regexp = "WORD|PHRASE|PARAPHRASE|PRONUNCIATION|GRAMMAR_POINT",
                message = "targetType 必须为 WORD/PHRASE/PARAPHRASE/PRONUNCIATION/GRAMMAR_POINT"
        )
        String targetType,
        /** 目标内容 ID */
        @NotNull UUID targetId,
        /** 关联备注，最多 500 字 */
        @Size(max = 500, message = "note 不能超过 500 个字符") String note
) {}
