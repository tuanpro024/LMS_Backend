package com.lms.videocourse.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlashcardProgressEvent {
    private String eventId;
    private String userId;
    private String studySetId;
    private int learnedCards;
    private int totalCards;
    private double progressPercentage;
    private boolean completed;
    private Instant completedAt;
    private Instant occurredAt;
}
