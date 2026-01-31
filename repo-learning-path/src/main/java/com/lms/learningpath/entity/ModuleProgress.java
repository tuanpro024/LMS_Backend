package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on individual step modules.
 * Each module within a step can be tracked separately.
 */
@Entity
@Table(name = "module_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "step_module_id" }), indexes = {
                @Index(name = "idx_user_step", columnList = "user_id, step_id"),
                @Index(name = "idx_user_status", columnList = "user_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String stepModuleId; // FK to StepModule

    @Column(nullable = false, length = 26)
    private String stepId; // Denormalized for easier querying

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedItems = 0; // Số items đã hoàn thành

    @Column(nullable = false)
    @Builder.Default
    private Integer totalItems = 0; // Tổng số items

    private Integer score; // Điểm cuối cùng (0-100)

    @Column(nullable = false)
    @Builder.Default
    private Integer totalAttempts = 0; // Số lần thử

    @Column(nullable = false)
    @Builder.Default
    private Integer studyTimeSeconds = 0; // Tổng thời gian học (giây)

    private Instant firstStartedAt;
    private Instant lastAttemptAt;
    private Instant startedAt;
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON metadata (chi tiết progress, answers, etc.)

    // Helper methods
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public double getProgressPercentage() {
        if (totalItems == 0)
            return 0.0;
        return (completedItems * 100.0) / totalItems;
    }
}
