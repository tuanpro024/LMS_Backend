package com.lms.onllearning.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OnlineCourseResponse(
    String id,
    String name,
    String thumbnail,
    String description,
    String syllabusId,
    BigDecimal price,
    double rating,
    Instant createdAt,
    Instant updatedAt
) {}
