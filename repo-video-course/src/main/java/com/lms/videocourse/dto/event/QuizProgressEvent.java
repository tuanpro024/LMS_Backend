package com.lms.videocourse.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizProgressEvent {
    private String eventId;
    private String userId;
    private String studySetId;
    private Integer completedQuizzes;
    private Integer totalQuizzes;
    private Double progressPercentage;
    private Boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
