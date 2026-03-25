package com.lms.onllearning.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OnlineCourseResponse(
        String id,
        String code,
        String name,
        String courseType,
        String level,
        Integer totalLessons,
        String thumbnail,
        String description,
        String syllabusId,
        BigDecimal price,
        double rating,
        boolean cmsSynced,
        Instant createdAt,
        Instant updatedAt) {
}
