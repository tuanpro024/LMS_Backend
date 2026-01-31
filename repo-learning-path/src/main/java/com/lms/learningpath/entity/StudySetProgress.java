package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on a StudySet.
 * Used to determine if next StudySet should be unlocked.
 */
@Entity
@Table(name = "study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "study_set_id" }), indexes = {
                @Index(name = "idx_user_folder", columnList = "user_id, folder_id"),
                @Index(name = "idx_user_package", columnList = "user_id, package_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String studySetId; // FK to StudySet (Content Common)

    @Column(nullable = false, length = 26)
    private String folderId; // Denormalized from StudySet

    @Column(nullable = false, length = 26)
    private String packageId; // Denormalized from Folder

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedModules = 0; // Số module đã hoàn thành

    @Column(nullable = false)
    @Builder.Default
    private Integer totalModules = 0; // Tổng số module

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredCompletedModules = 0; // Số module bắt buộc đã hoàn thành

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRequiredModules = 0; // Tổng số module bắt buộc

    private Instant firstStartedAt;
    private Instant completedAt;

    // Helper methods
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public boolean canUnlockNext() {
        // Hoàn thành tất cả module bắt buộc
        return requiredCompletedModules >= totalRequiredModules;
    }

    public double getProgressPercentage() {
        if (totalModules == 0)
            return 0.0;
        return (completedModules * 100.0) / totalModules;
    }
}
