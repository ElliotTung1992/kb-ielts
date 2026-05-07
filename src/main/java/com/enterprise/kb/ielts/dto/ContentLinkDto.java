package com.enterprise.kb.ielts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * 关联内容响应体，正向/反向查询共用。
 * targetSummary = 目标内容摘要（单词原形 / 核心表达 / 语法标题等）
 * targetDetail  = 目标内容补充说明（中文释义 / 同义词 / 分类）
 * sourceSummary / sourceDetail 仅反向查询时有值（跨技能内容被哪些技能内容引用）
 */
@Getter
@Setter
public class ContentLinkDto {

    private UUID linkId;
    private String linkType;
    /** 正向查询：目标跨技能类型；反向查询：来源技能类型 */
    private String targetType;
    private UUID targetId;
    private String note;
    private Instant createdAt;

    /** 目标内容摘要，用于正向关联列表展示 */
    private String targetSummary;
    /** 目标内容补充说明 */
    private String targetDetail;

    /** 正向查询：来源技能类型（sourceType + sourceId）；反向查询时有值 */
    private String sourceType;
    /** 反向查询时：来源技能内容的 ID */
    private UUID sourceId;
    /** 反向查询时：来源技能内容的标题 */
    private String sourceSummary;
    /** 反向查询时：来源技能内容的补充说明（section / task number 等） */
    private String sourceDetail;
}
