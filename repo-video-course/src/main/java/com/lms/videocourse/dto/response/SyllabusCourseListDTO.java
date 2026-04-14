package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Summary DTO for the course list view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusCourseListDTO {
    private String id;
    private String cmsCourseId;
    private String code;
    private String name;
    private String courseType;
    private String level;
    private BigDecimal price;
    private Integer totalLessons;
    private String syllabusId;
    private String syllabusName;
    private Instant lastSyncedAt;
}
