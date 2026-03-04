package com.lms.videocourse.dto.response;

import com.lms.videocourse.entity.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VideoWatchProgressResponse {
    private String id;
    private String videoModuleId;
    private String stepId;
    private ProgressStatus status;

    private Integer watchedSeconds;
    private Integer totalDurationSeconds;
    private Double watchPercent; // 0-100

    private Integer lastPositionSeconds; // For resume playback

    private Boolean autoCompleted; // true if completed via 80% threshold

    private Instant firstStartedAt;
    private Instant lastWatchedAt;
    private Instant completedAt;
}
