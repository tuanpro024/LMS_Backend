package com.lms.quiz.dto.response;

import com.lms.quiz.entity.enums.StudySetProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStudySetProgressResponse {
    private String id;
    private String userId;
    private String studySetId;
    private String studySetTitle;
    private StudySetProgressStatus status;
    private Integer completedQuizzes;
    private Integer totalQuizzes;
    private Double progressPercentage;
    private Instant firstStartedAt;
    private Instant completedAt;
}
