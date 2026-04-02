package com.lms.videocourse.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KanjiProgressEvent {
    private String eventId;
    private String userId;
    private String studySetId;
    private Integer learnedLessons;
    private Integer totalLessons;
    private Double progressPercentage;
    private Boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
