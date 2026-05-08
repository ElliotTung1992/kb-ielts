package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class IeltsStudyProfile {
    /** 主键；单用户模式下通常只保留一条记录 */
    private UUID id;
    /** 目标总分 */
    private BigDecimal targetOverallScore;
    /** 目标听力分 */
    private BigDecimal targetListeningScore;
    /** 目标阅读分 */
    private BigDecimal targetReadingScore;
    /** 目标写作分 */
    private BigDecimal targetWritingScore;
    /** 目标口语分 */
    private BigDecimal targetSpeakingScore;
    /** 当前总分或最近一次模考总分 */
    private BigDecimal currentOverallScore;
    /** 预计考试日期 */
    private LocalDate examDate;
    /** 每日可学习分钟数 */
    private Integer dailyMinutes;
    /** 考试类型：ACADEMIC / GENERAL */
    private String trainingType;
    /** 口音偏好：UK / US / MIXED 等 */
    private String accentPreference;
    /** 重点技能，逗号分隔，如 READING,WRITING */
    private String focusSkills;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
}
