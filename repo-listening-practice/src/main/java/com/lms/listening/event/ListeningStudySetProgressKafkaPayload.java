package com.lms.listening.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningStudySetProgressKafkaPayload {
    private String eventId;
    private String userId;
    private String studySetId;
    private int completedVideos;
    private int totalVideos;
    private double progressPercentage;
    private boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
