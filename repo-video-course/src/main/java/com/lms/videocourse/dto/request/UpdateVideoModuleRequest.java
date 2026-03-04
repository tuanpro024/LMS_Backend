package com.lms.videocourse.dto.request;

import lombok.Data;

@Data
public class UpdateVideoModuleRequest {
    private String title;
    private String description;
    private Integer moduleOrder;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration;
    private String subtitles;
    private String videoCode;
    private String moduleType;
    private String contentSetId;
    private Boolean isRequired;
    private Boolean isActive;
}
