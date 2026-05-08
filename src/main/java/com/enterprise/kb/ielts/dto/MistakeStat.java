package com.enterprise.kb.ielts.dto;

/**
 * 错因统计项。
 */
public record MistakeStat(
        /** 错因类型 */
        String mistakeType,
        /** 出现次数 */
        long count
) {}
