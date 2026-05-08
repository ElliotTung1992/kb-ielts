package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.dto.MistakeStat;
import com.enterprise.kb.ielts.model.IeltsMistakeLog;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface IeltsMistakeLogMapper {

    @Insert("""
            INSERT INTO ielts_mistake_logs (id, content_type, content_id, record_id, mistake_type, note, created_at)
            VALUES (#{id,jdbcType=OTHER}, #{contentType}, #{contentId,jdbcType=OTHER}, #{recordId,jdbcType=OTHER},
                    #{mistakeType}, #{note}, #{createdAt})
            """)
    int insert(IeltsMistakeLog log);

    @Select("""
            SELECT mistake_type AS mistakeType, COUNT(*) AS count
            FROM ielts_mistake_logs
            WHERE created_at >= #{since}
            GROUP BY mistake_type
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<MistakeStat> findStats(@Param("since") Instant since, @Param("limit") int limit);

    @Select("""
            SELECT * FROM ielts_mistake_logs
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<IeltsMistakeLog> findRecent(@Param("limit") int limit);

    @Delete("DELETE FROM ielts_mistake_logs WHERE id = #{id,jdbcType=OTHER}")
    int deleteById(@Param("id") UUID id);
}
