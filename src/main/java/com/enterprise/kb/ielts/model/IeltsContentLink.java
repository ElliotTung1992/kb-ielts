package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsContentLink {

    /** 主键 */
    private UUID id;
    /** 来源技能内容类型：LISTENING / READING / WRITING / SPEAKING */
    private String sourceType;
    /** 来源内容主键 */
    private UUID sourceId;
    /** 目标跨技能内容类型：WORD / PHRASE / PARAPHRASE / PRONUNCIATION / GRAMMAR_POINT */
    private String targetType;
    /** 目标内容主键 */
    private UUID targetId;
    /** 关联类型：历史字段，前端已不再使用，保留兼容旧数据 */
    private String linkType;
    /** 补充说明 */
    private String note;
    /** 创建时间 */
    private Instant createdAt;
}
