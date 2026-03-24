package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * One submission of a quiz by a user.
 */
@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_attempt_user_quiz", columnList = "user_id, quiz_id"),
        @Index(name = "idx_attempt_quiz", columnList = "quiz_id"),
        @Index(name = "idx_attempt_study_set", columnList = "study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;

    @Column(name = "quiz_id", nullable = false, length = 26)
    private String quizId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Integer totalPoints;

    @Column(nullable = false)
    private Integer earnedPoints;

    @Column(nullable = false)
    private Double scorePercentage;

    @Column(nullable = false)
    private Boolean passed;

    private Integer timeTakenSeconds;

    @Column(nullable = false)
    private Instant submittedAt;
}
