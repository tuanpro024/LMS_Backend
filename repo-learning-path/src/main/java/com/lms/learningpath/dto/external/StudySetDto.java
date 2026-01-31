package com.lms.learningpath.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for StudySet data fetched from external modules (flashcard, kanji-origin,
 * writing, etc.).
 * This is a simplified version containing only the fields needed for learning
 * path display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySetDto {
    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private Boolean isPrivate;
    private String userId;
    private Integer estimatedMinutes;
    private Long itemCount; // Number of cards/words in the study set
}
