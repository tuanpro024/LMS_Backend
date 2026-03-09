package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVideoCourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String thumbnail;

    private Integer estimatedHours;

    @NotBlank(message = "StudySet ID is required")
    private String studySetId;

    private Integer contentIndex;
}
