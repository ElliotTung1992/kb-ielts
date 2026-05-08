package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.model.IeltsStudyProfile;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface IeltsStudyProfileMapper {

    @Select("""
            SELECT * FROM ielts_study_profile
            ORDER BY created_at
            LIMIT 1
            """)
    Optional<IeltsStudyProfile> findOne();

    @Insert("""
            INSERT INTO ielts_study_profile (
                id, target_overall_score, target_listening_score, target_reading_score,
                target_writing_score, target_speaking_score, current_overall_score,
                exam_date, daily_minutes, training_type, accent_preference, focus_skills,
                created_at, updated_at
            ) VALUES (
                #{id,jdbcType=OTHER}, #{targetOverallScore}, #{targetListeningScore}, #{targetReadingScore},
                #{targetWritingScore}, #{targetSpeakingScore}, #{currentOverallScore},
                #{examDate}, #{dailyMinutes}, #{trainingType}, #{accentPreference}, #{focusSkills},
                #{createdAt}, #{updatedAt}
            )
            """)
    int insert(IeltsStudyProfile profile);

    @Update("""
            UPDATE ielts_study_profile SET
                target_overall_score = #{targetOverallScore},
                target_listening_score = #{targetListeningScore},
                target_reading_score = #{targetReadingScore},
                target_writing_score = #{targetWritingScore},
                target_speaking_score = #{targetSpeakingScore},
                current_overall_score = #{currentOverallScore},
                exam_date = #{examDate},
                daily_minutes = #{dailyMinutes},
                training_type = #{trainingType},
                accent_preference = #{accentPreference},
                focus_skills = #{focusSkills},
                updated_at = #{updatedAt}
            WHERE id = #{id,jdbcType=OTHER}
            """)
    int update(IeltsStudyProfile profile);
}
