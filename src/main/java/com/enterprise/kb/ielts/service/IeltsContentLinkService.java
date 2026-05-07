package com.enterprise.kb.ielts.service;

import com.enterprise.kb.ielts.dto.AddLinkRequest;
import com.enterprise.kb.ielts.dto.ContentLinkDto;
import com.enterprise.kb.ielts.model.IeltsContentLink;

import java.util.List;
import java.util.UUID;

public interface IeltsContentLinkService {

    /** 正向查询：技能内容关联了哪些跨技能内容 */
    List<ContentLinkDto> listBySource(String sourceType, UUID sourceId);

    /** 反向查询：跨技能内容被哪些技能内容引用 */
    List<ContentLinkDto> listByTarget(String targetType, UUID targetId);

    /** 添加关联 */
    IeltsContentLink addLink(String sourceType, UUID sourceId, AddLinkRequest request);

    /** 删除单条关联 */
    void removeLink(UUID linkId);

    /** 删除某技能内容的全部关联（内容删除级联） */
    void removeBySource(String sourceType, UUID sourceId);

    /** 删除某跨技能内容作为目标的全部关联（内容删除级联） */
    void removeByTarget(String targetType, UUID targetId);
}
