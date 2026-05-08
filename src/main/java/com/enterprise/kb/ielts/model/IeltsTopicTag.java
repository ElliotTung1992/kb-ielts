package com.enterprise.kb.ielts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class IeltsTopicTag {
    /** 主键 */
    private UUID id;

    /** 标签名称，例如 education / environment */
    private String tagName;

    /** 标签分类，例如 topic / scenario / exam-theme */
    private String category;

    /** 适用技能，逗号分隔，例如 speaking,writing */
    private String skillTags;

    /** 标签说明 */
    private String description;

    /** 使用次数，用于后续统计和排序 */
    private Integer usageCount;

    /** 是否启用 */
    private Boolean enabled;

    /** 创建时间 */
    private Instant createdAt;

    /** 更新时间 */
    private Instant updatedAt;
}
