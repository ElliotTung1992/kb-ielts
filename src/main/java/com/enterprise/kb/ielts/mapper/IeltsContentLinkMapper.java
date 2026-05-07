package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.dto.ContentLinkDto;
import com.enterprise.kb.ielts.model.IeltsContentLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface IeltsContentLinkMapper {

    Optional<IeltsContentLink> findById(@Param("id") UUID id);

    /** 正向查询：技能内容关联了哪些跨技能内容（含目标详情快照） */
    List<ContentLinkDto> findBySource(@Param("sourceType") String sourceType,
                                      @Param("sourceId") UUID sourceId);

    /** 反向查询：跨技能内容被哪些技能内容引用（含来源详情快照） */
    List<ContentLinkDto> findByTarget(@Param("targetType") String targetType,
                                      @Param("targetId") UUID targetId);

    /** 统计某跨技能内容的引用数 */
    int countByTarget(@Param("targetType") String targetType,
                      @Param("targetId") UUID targetId);

    void insert(IeltsContentLink link);

    void deleteById(@Param("id") UUID id);

    /** 删除某技能内容的全部关联（内容删除时级联清理） */
    void deleteBySource(@Param("sourceType") String sourceType,
                        @Param("sourceId") UUID sourceId);

    /** 删除某跨技能内容作为目标的全部关联（内容删除时级联清理） */
    void deleteByTarget(@Param("targetType") String targetType,
                        @Param("targetId") UUID targetId);
}
