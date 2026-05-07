package com.enterprise.kb.ielts.mapper;

import com.enterprise.kb.ielts.dto.StudyPlanItem;
import com.enterprise.kb.ielts.model.IeltsDailyPlanItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface IeltsDailyPlanItemMapper {

    List<StudyPlanItem> findItemsByPlanId(@Param("planId") UUID planId);

    int countByPlanId(@Param("planId") UUID planId);

    int countCompletedByPlanId(@Param("planId") UUID planId);

    int batchInsert(@Param("items") List<IeltsDailyPlanItem> items);

    int updateRecordIdByContent(@Param("planId") UUID planId,
                                @Param("contentType") String contentType,
                                @Param("contentId") UUID contentId,
                                @Param("recordId") UUID recordId);

    int markCompletedByRecordId(@Param("planId") UUID planId,
                                @Param("recordId") UUID recordId,
                                @Param("completedAt") java.time.Instant completedAt);

    int markCompletedByContent(@Param("planId") UUID planId,
                               @Param("contentType") String contentType,
                               @Param("contentId") UUID contentId,
                               @Param("recordId") UUID recordId,
                               @Param("completedAt") java.time.Instant completedAt);
}
