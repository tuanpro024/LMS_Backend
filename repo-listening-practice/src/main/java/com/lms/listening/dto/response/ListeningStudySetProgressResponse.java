package com.lms.listening.dto.response;

import com.lms.listening.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ListeningStudySetProgressResponse {
    private String id;
    private String userId;
    private String studySetId;
    private ProgressStatus status;
    private Integer completedVideos;
    private Integer totalVideos;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
