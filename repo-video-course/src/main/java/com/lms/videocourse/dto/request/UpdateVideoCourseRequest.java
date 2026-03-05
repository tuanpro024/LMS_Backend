package com.lms.videocourse.dto.request;

import lombok.Data;

@Data
public class UpdateVideoCourseRequest {
    private String title;
    private String description;
    private String thumbnail;
    private Integer estimatedHours;
    private Boolean isActive;
    private Integer contentIndex;
}
