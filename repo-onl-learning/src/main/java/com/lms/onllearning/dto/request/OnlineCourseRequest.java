package com.lms.onllearning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OnlineCourseRequest(
    @NotBlank @Size(max = 255)
    String name,

    String thumbnail,

    String description,

    @Size(max = 26)
    String syllabusId,

    @DecimalMin("0") BigDecimal price,

    @DecimalMin("0.0") @DecimalMax("5.0")
    Double rating
) {}
