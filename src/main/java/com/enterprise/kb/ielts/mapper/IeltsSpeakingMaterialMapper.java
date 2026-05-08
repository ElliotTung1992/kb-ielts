package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsSpeakingMaterial;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IeltsSpeakingMaterialMapper {

    @Select("""
            <script>
            SELECT * FROM ielts_speaking_materials
            <where>
                <if test="category != null and category != ''">
                    category = #{category}
                </if>
            </where>
            ORDER BY created_at DESC
            LIMIT #{limit}
            </script>
            """)
    List<IeltsSpeakingMaterial> findRecent(@Param("category") String category, @Param("limit") int limit);

    @Insert("""
            INSERT INTO ielts_speaking_materials (id, category, title, content, topic_tags, usable_for_parts, created_at, updated_at)
            VALUES (#{id,jdbcType=OTHER}, #{category}, #{title}, #{content}, #{topicTags}, #{usableForParts}, #{createdAt}, #{updatedAt})
            """)
    int insert(IeltsSpeakingMaterial material);
}
