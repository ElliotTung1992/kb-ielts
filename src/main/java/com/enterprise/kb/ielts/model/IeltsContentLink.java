package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsContentLink {

    private UUID id;
    /** 来源技能内容类型：LISTENING / READING / WRITING / SPEAKING */
    private String sourceType;
    /** 来源内容主键 */
    private UUID sourceId;
    /** 目标跨技能内容类型：WORD / PHRASE / PARAPHRASE / PRONUNCIATION / GRAMMAR_POINT */
    private String targetType;
    /** 目标内容主键 */
    private UUID targetId;
    /** 关联类型：vocabulary / paraphrase / grammar / pronunciation / signal */
    private String linkType;
    /** 补充说明 */
    private String note;
    private Instant createdAt;
}
