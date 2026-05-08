package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsMistakeLog {
    /** 主键 */
    private UUID id;
    /** 内容类型：WORD / PHRASE / READING / WRITING 等 */
    private String contentType;
    /** 对应内容表主键 */
    private UUID contentId;
    /** 关联学习记录 ID；计划外错因可为空 */
    private UUID recordId;
    /** 错因类型：VOCABULARY / PARAPHRASE / GRAMMAR / PRONUNCIATION / QUESTION_READING / LOCATION / TIME / SPELLING / EXPRESSION */
    private String mistakeType;
    /** 错因备注 */
    private String note;
    /** 创建时间 */
    private Instant createdAt;
}
