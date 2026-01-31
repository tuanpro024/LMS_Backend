package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on an entire Learning Path.
 * Aggregates progress from all steps in the path.
 */
@Entity
@Table(name = "learning_path_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "learning_path_id" }), indexes = {
                @Index(name = "idx_user_study_set", columnList = "user_id, study_set_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String learningPathId; // FK to LearningPath

    @Column(nullable = false, length = 26)
    private String studySetId; // Denormalized from LearningPath

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedSteps = 0; // Số step đã hoàn thành

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSteps = 0; // Tổng số step

    @Column(length = 26)
    private String currentStepId; // Current step user is working on

    private Instant firstStartedAt;
    private Instant completedAt;

    // Helper methods
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public double getProgressPercentage() {
        if (totalSteps == 0)
            return 0.0;
        return (completedSteps * 100.0) / totalSteps;
    }
}
