package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsTopicTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface IeltsTopicTagMapper {

    @Select("""
            <script>
            SELECT * FROM ielts_topic_tags
            <where>
                <if test="keyword != null and keyword != ''">
                    AND (tag_name ILIKE CONCAT('%', #{keyword}, '%')
                         OR description ILIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="category != null and category != ''">
                    AND category = #{category}
                </if>
                <if test="skill != null and skill != ''">
                    AND skill_tags ILIKE CONCAT('%', #{skill}, '%')
                </if>
                <if test="enabled != null">
                    AND enabled = #{enabled}
                </if>
            </where>
            ORDER BY enabled DESC, usage_count DESC, tag_name
            LIMIT #{limit}
            </script>
            """)
    List<IeltsTopicTag> findAll(@Param("keyword") String keyword,
                                @Param("category") String category,
                                @Param("skill") String skill,
                                @Param("enabled") Boolean enabled,
                                @Param("limit") int limit);

    @Select("SELECT * FROM ielts_topic_tags WHERE id = #{id,jdbcType=OTHER}")
    Optional<IeltsTopicTag> findById(@Param("id") UUID id);

    @Insert("""
            INSERT INTO ielts_topic_tags
                (id, tag_name, category, skill_tags, description, usage_count, enabled, created_at, updated_at)
            VALUES
                (#{id,jdbcType=OTHER}, #{tagName}, #{category}, #{skillTags}, #{description},
                 #{usageCount}, #{enabled}, #{createdAt}, #{updatedAt})
            """)
    int insert(IeltsTopicTag tag);

    @Update("""
            UPDATE ielts_topic_tags SET
                tag_name = #{tagName},
                category = #{category},
                skill_tags = #{skillTags},
                description = #{description},
                usage_count = #{usageCount},
                enabled = #{enabled},
                updated_at = #{updatedAt}
            WHERE id = #{id,jdbcType=OTHER}
            """)
    int update(IeltsTopicTag tag);

    @Delete("DELETE FROM ielts_topic_tags WHERE id = #{id,jdbcType=OTHER}")
    int deleteById(@Param("id") UUID id);
}
