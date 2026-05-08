package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class IeltsReviewLog {

    /** 主键 */
    private UUID id;
    /** 关联 ielts_study_records */
    private UUID recordId;
    /** 本次评分：AGAIN / HARD / GOOD / EASY */
    private String rating;
    /** 复习前状态 */
    private String beforeStatus;
    /** 复习后状态 */
    private String afterStatus;
    /** 复习前间隔天数 */
    private Integer beforeIntervalDays;
    /** 复习后间隔天数 */
    private Integer afterIntervalDays;
    /** 复习前重复次数 */
    private Integer beforeRepetitionCount;
    /** 复习后重复次数 */
    private Integer afterRepetitionCount;
    /** 复习前难易系数 */
    private BigDecimal beforeEaseFactor;
    /** 复习后难易系数 */
    private BigDecimal afterEaseFactor;
    /** 复习时间 */
    private Instant reviewedAt;
}
