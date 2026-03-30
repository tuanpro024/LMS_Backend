package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.quiz.entity.enums.StudySetProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Aggregated per-user progress for an entire Study Set consisting of multiple Quizzes.
 */
@Entity
@Table(name = "user_quiz_study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "study_set_id" }), indexes = {
        @Index(name = "idx_quiz_ss_prog_user", columnList = "user_id"),
        @Index(name = "idx_quiz_ss_prog_study_set", columnList = "study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizStudySetProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedQuizzes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalQuizzes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private StudySetProgressStatus status = StudySetProgressStatus.NOT_STARTED;

    private Instant firstStartedAt;

    private Instant completedAt;
}
