package com.lms.multimedia.dto.response;

import com.lms.multimedia.entity.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private VideoStatus status;
    private Integer duration;
    private String thumbnailPath;
    private String karaokePath;
    private String studySetId;
    private String createdAt;
    private String updatedAt;
}
