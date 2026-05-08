package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsSpeakingMaterial {
    /** 主键 */
    private UUID id;
    /** 素材类别：experience / person / place / object / event 等 */
    private String category;
    /** 素材标题 */
    private String title;
    /** 素材正文 */
    private String content;
    /** 话题标签，逗号分隔 */
    private String topicTags;
    /** 适用口语 Part，逗号分隔，如 1,2,3 */
    private String usableForParts;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;
}
