package com.lms.onllearning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OnlineCourseRequest(
        @Size(max = 50) String code,

        @NotBlank @Size(max = 255) String name,

        @Size(max = 20) String courseType,

        @Size(max = 20) String level,

        Integer totalLessons,

        String thumbnail,

        String description,

        @Size(max = 26) String syllabusId,

        @DecimalMin("0") BigDecimal price,

        @DecimalMin("0.0") @DecimalMax("5.0") Double rating) {
}
