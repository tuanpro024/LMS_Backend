package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Aggregated per-user progress for a quiz.
 */
@Entity
@Table(name = "user_quiz_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "quiz_id" }), indexes = {
        @Index(name = "idx_progress_user", columnList = "user_id"),
        @Index(name = "idx_progress_quiz", columnList = "quiz_id"),
        @Index(name = "idx_progress_study_set", columnList = "study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;

    @Column(name = "quiz_id", nullable = false, length = 26)
    private String quizId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptsCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double latestScorePercentage = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double bestScorePercentage = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    private String latestAttemptId;

    private String bestAttemptId;

    private Instant firstAttemptAt;

    private Instant lastAttemptAt;

    private Instant completedAt;
}
