package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsMockTestSection;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface IeltsMockTestSectionMapper {

    @Select("SELECT * FROM ielts_mock_test_sections WHERE mock_test_id = #{mockTestId,jdbcType=OTHER} ORDER BY skill")
    List<IeltsMockTestSection> findByMockTestId(@Param("mockTestId") UUID mockTestId);

    @Select("""
            SELECT s.*
            FROM ielts_mock_test_sections s
            JOIN ielts_mock_tests t ON t.id = s.mock_test_id
            ORDER BY t.test_date ASC, s.skill
            """)
    List<IeltsMockTestSection> findAllForTrend();

    @Insert("""
            INSERT INTO ielts_mock_test_sections (
                id, mock_test_id, skill, score, raw_score, question_count, wrong_count, main_issues, created_at
            ) VALUES (
                #{id,jdbcType=OTHER}, #{mockTestId,jdbcType=OTHER}, #{skill}, #{score}, #{rawScore},
                #{questionCount}, #{wrongCount}, #{mainIssues}, #{createdAt}
            )
            """)
    int insert(IeltsMockTestSection section);
}
