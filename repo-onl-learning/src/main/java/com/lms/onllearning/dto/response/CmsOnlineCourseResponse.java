package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CmsOnlineCourseResponse(
        String id,
        String code,
        String name,
        String type,
        String level,
        @JsonProperty("total_lessons") Integer totalLessons,
        BigDecimal price) {
}
