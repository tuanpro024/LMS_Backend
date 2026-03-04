package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Defines sequential unlock rules for VideoSteps.
 * By default: Step 1 is always unlocked.
 * Step N requires Step N-1 to be COMPLETED (all required modules watched at ≥
 * 80%).
 * Mirrors StepUnlockRule from repo-learning-path.
 */
@Entity
@Table(name = "video_step_unlock_rules", indexes = {
        @Index(name = "idx_vsur_step", columnList = "step_id"),
        @Index(name = "idx_vsur_required_step", columnList = "required_step_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStepUnlockRule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String stepId; // The step to unlock

    @Column(length = 26)
    private String requiredStepId; // Step that must be completed first (null = always unlocked)

    @Column(nullable = false)
    @Builder.Default
    private Boolean requireAllModules = true; // Must complete all required modules?

    @Column(nullable = false)
    @Builder.Default
    private Integer minimumWatchPercent = 80; // Minimum watch % to count as completed (default 80%)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
