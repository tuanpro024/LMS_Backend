package com.lms.flashcard.dto.response;

import com.lms.flashcard.entity.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardStudySetProgressResponse {
    private String id;
    private String userId;
    private String studySetId;
    private String studySetTitle;
    private ProgressStatus status;
    private Integer learnedCards;
    private Integer totalCards;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
