package com.lms.aipractice.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.aipractice.entity.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * One student attempt at a StudySet of AI practice items.
 */
@Entity
@Table(name = "ai_practice_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPracticeAttempt extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    /** Accumulated score across all graded answers in this attempt */
    @Column(name = "total_score")
    private Double totalScore;

    /** Sum of max_score across all items */
    @Column(name = "max_score")
    private Double maxScore;

    /** totalScore / maxScore * 100, 0-100 */
    @Column(name = "progress_percent")
    private Double progressPercent;
}
