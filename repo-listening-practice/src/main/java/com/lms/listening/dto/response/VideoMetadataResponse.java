package com.lms.listening.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadataResponse {

    private String id;
    private String studySetId;
    private String videoCode;
    private String name;
    private String description;
    private String thumbnailPath;
    private Integer duration;
    private Integer displayOrder;
    private String playlistUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
