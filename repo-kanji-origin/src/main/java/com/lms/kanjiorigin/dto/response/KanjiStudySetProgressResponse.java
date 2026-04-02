package com.lms.kanjiorigin.dto.response;

import com.lms.kanjiorigin.entity.enums.StudySetProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiStudySetProgressResponse {

    private String id;
    private String userId;
    private String studySetId;
    private StudySetProgressStatus status;
    private Integer learnedLessons;
    private Integer totalLessons;
    private Double progressPercentage;
    private Boolean completed;
    private Instant firstStartedAt;
    private Instant completedAt;
}