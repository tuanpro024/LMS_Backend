package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.videocourse.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on a specific VideoStep.
 * Used to determine if the next VideoStep should be unlocked.
 * Mirrors StepProgress from repo-learning-path.
 */
@Entity
@Table(name = "video_step_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "step_id" }), indexes = {
                @Index(name = "idx_vsp_user_study_set", columnList = "user_id, study_set_id"),
                @Index(name = "idx_vsp_user_step", columnList = "user_id, step_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStepProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String stepId; // FK to VideoStep

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId; // Denormalized for easier querying

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedModules = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalModules = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredCompletedModules = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRequiredModules = 0;

    private Instant firstStartedAt;
    private Instant completedAt;

    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public boolean canUnlockNext() {
        return status == ProgressStatus.COMPLETED;
    }

    public double getProgressPercentage() {
        if (totalModules == 0)
            return 0.0;
        return (completedModules * 100.0) / totalModules;
    }
}
