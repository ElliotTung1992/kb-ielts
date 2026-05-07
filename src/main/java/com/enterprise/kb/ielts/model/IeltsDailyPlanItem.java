package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsDailyPlanItem {

    /** 主键 */
    private UUID id;
    /** 所属每日计划 ID，对应 ielts_daily_plans.id */
    private UUID planId;
    /** 内容类型：WORD / PHRASE / PARAPHRASE / PRONUNCIATION / GRAMMAR_POINT / GRAMMAR_EXERCISE / SPEAKING / LISTENING / READING / WRITING */
    private String contentType;
    /** 对应内容表的主键，与 contentType 组合定位具体内容 */
    private UUID contentId;
    /** 对应学习记录 ID；NEW 条目在开始学习前可为空，开始学习后回填 */
    private UUID recordId;
    /** 学习模式：NEW=新学，REVIEW=到期复习 */
    private String studyMode;
    /** 条目状态：PENDING=待完成，COMPLETED=已完成，SKIPPED=已跳过 */
    private String status;
    /** 计划生成时保存的展示摘要，避免内容变化导致当天计划列表漂移 */
    private String summary;
    /** 当天计划内排序，复习项优先，新学项置后 */
    private Integer sortOrder;
    /** 完成时间；status=COMPLETED 时写入 */
    private Instant completedAt;
    /** 明细创建时间 */
    private Instant createdAt;
}
