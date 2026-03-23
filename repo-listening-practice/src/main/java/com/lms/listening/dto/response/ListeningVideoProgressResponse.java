package com.lms.listening.dto.response;

import com.lms.listening.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ListeningVideoProgressResponse {
    private String id;
    private String userId;
    private String studySetId;
    private String videoCode;
    private ProgressStatus status;
    private Integer watchedSeconds;
    private Integer totalDurationSeconds;
    private Double watchPercent;
    private Integer lastPositionSeconds;
    private Boolean isCompleted;
    private Instant firstStartedAt;
    private Instant lastWatchedAt;
    private Instant completedAt;
}
