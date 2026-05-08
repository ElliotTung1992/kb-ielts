package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsWritingSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IeltsWritingSubmissionMapper {

    @Select("SELECT * FROM ielts_writing_submissions ORDER BY created_at DESC LIMIT #{limit}")
    List<IeltsWritingSubmission> findRecent(@Param("limit") int limit);

    @Insert("""
            INSERT INTO ielts_writing_submissions (
                id, task_id, original_text, revised_text, target_band, estimated_band,
                feedback, criteria_scores, created_at, updated_at
            ) VALUES (
                #{id,jdbcType=OTHER}, #{taskId,jdbcType=OTHER}, #{originalText}, #{revisedText},
                #{targetBand}, #{estimatedBand}, #{feedback}, #{criteriaScores}, #{createdAt}, #{updatedAt}
            )
            """)
    int insert(IeltsWritingSubmission submission);
}
