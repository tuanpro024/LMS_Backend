package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.videocourse.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user watch progress on an individual VideoModule.
 * Auto-completes at 80% watch time (watchedSeconds / duration >= 0.80).
 * Mirrors ModuleProgress from repo-learning-path with video-specific fields.
 */
@Entity
@Table(name = "video_watch_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "video_module_id" }), indexes = {
                @Index(name = "idx_vwp_user_step", columnList = "user_id, step_id"),
                @Index(name = "idx_vwp_user_module", columnList = "user_id, video_module_id"),
                @Index(name = "idx_vwp_user_status", columnList = "user_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoWatchProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String videoModuleId; // FK to VideoModule

    @Column(nullable = false, length = 26)
    private String stepId; // Denormalized for easier querying

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    // ===== Watch time tracking =====

    @Column(nullable = false)
    @Builder.Default
    private Integer watchedSeconds = 0; // How many seconds user has watched

    @Column(nullable = false)
    @Builder.Default
    private Integer totalDurationSeconds = 0; // Total video duration (from VideoModule.duration)

    @Column(nullable = false)
    @Builder.Default
    private Double watchPercent = 0.0; // watchedSeconds / totalDurationSeconds * 100

    // ===== Resume position =====

    @Column(nullable = false)
    @Builder.Default
    private Integer lastPositionSeconds = 0; // Last playback position (for resume)

    // ===== Completion tracking =====

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoCompleted = false; // true if completed via 80% threshold

    private Instant firstStartedAt;
    private Instant lastWatchedAt;
    private Instant completedAt;

    /**
     * Calculates and updates watchPercent.
     * Auto-completes when watch percent >= 80.
     */
    public void updateWatchTime(int newWatchedSeconds) {
        this.watchedSeconds = Math.max(this.watchedSeconds, newWatchedSeconds); // always move forward
        if (this.totalDurationSeconds > 0) {
            this.watchPercent = (this.watchedSeconds * 100.0) / this.totalDurationSeconds;
        }
        this.lastWatchedAt = Instant.now();
        this.status = ProgressStatus.IN_PROGRESS;
    }

    /**
     * Returns true if watch time qualifies for auto-complete (>= 80%).
     */
    public boolean qualifiesForAutoComplete() {
        return watchPercent >= 80.0 && status != ProgressStatus.COMPLETED;
    }

    /**
     * Marks this progress as auto-completed (triggered at 80% watch).
     */
    public void autoComplete() {
        this.status = ProgressStatus.COMPLETED;
        this.autoCompleted = true;
        this.completedAt = Instant.now();
    }
}
