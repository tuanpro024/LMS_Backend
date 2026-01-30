package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on individual modules.
 * Users can learn modules in any order within a StudySet.
 */
@Entity
@Table(name = "module_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "module_id" }), indexes = {
                @Index(name = "idx_user_studyset", columnList = "user_id, study_set_id"),
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

    @Column(nullable = false, length = 26, name = "module_id")
    private String studySetModuleId; // FK to StudySetModule

    @Column(nullable = false, length = 26)
    private String studySetId; // Denormalized for easier querying

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
    private Integer studyTimeSeconds = 0; // Tổng thời gian học (giây)

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
