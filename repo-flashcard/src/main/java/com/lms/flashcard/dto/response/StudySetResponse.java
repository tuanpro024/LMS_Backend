package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySetResponse {

    private String id;
    private String title;
    private String description;
    private boolean isPrivate;
    private String userId;
    private double progress;
    private List<CardResponse> cards;
    private Instant createdAt;
    private Instant updatedAt;
}
