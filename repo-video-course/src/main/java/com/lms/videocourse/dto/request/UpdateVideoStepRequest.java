package com.lms.videocourse.dto.request;

import lombok.Data;

@Data
public class UpdateVideoStepRequest {
    private String title;
    private String description;
    private Integer stepOrder;
    private String icon;
    private String color;
    private Integer estimatedMinutes;
    private Boolean isRequired;
    private Boolean isActive;
}
