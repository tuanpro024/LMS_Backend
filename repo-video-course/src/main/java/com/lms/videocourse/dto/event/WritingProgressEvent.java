package com.lms.videocourse.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WritingProgressEvent {
    private String eventId;
    private String userId;
    private String studySetId;
    private Integer learnedWords;
    private Integer totalWords;
    private Double progressPercentage;
    private Boolean completed;
    private Instant occurredAt;
    
    // Default mapped to completedAt if possible
    private Instant completedAt;
}
