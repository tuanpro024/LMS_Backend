package com.lms.videocourse.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a single course from CMS courses/list API.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsCourseDTO {

    private String id;

    private String code;

    private String name;

    private String type;

    private String level;

    @JsonProperty("total_lessons")
    private Integer totalLessons;

    private String price;

    @JsonProperty("syllabus_id")
    private String syllabusId;
}
