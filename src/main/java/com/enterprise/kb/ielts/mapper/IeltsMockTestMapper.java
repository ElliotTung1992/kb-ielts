package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsMockTest;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface IeltsMockTestMapper {

    @Select("SELECT * FROM ielts_mock_tests ORDER BY test_date DESC, created_at DESC LIMIT #{limit}")
    List<IeltsMockTest> findRecent(@Param("limit") int limit);

    @Select("SELECT * FROM ielts_mock_tests ORDER BY test_date DESC, created_at DESC LIMIT 1")
    Optional<IeltsMockTest> findLatest();

    @Insert("""
            INSERT INTO ielts_mock_tests (id, test_date, source, overall_score, notes, next_focus, created_at, updated_at)
            VALUES (#{id,jdbcType=OTHER}, #{testDate}, #{source}, #{overallScore}, #{notes}, #{nextFocus}, #{createdAt}, #{updatedAt})
            """)
    int insert(IeltsMockTest mockTest);

    @Delete("DELETE FROM ielts_mock_tests WHERE id = #{id,jdbcType=OTHER}")
    int deleteById(@Param("id") UUID id);
}
