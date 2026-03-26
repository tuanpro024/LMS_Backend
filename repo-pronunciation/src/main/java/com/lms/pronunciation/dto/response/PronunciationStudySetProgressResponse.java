package com.lms.pronunciation.dto.response;

import com.lms.pronunciation.entity.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationStudySetProgressResponse {
    private String id;
    private String userId;
    private String studySetId;
    private ProgressStatus status;
    private Integer learnedItems;
    private Integer totalItems;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
