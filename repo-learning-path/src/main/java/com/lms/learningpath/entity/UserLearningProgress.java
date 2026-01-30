package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking user progress through an entire learning path (Package).
 * Tracks current position and overall completion.
 */
@Entity
@Table(name = "user_learning_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLearningProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(name = "package_id", nullable = false, length = 26)
    private String packageId; // Reference to Package (LearningPath)

    @Column(name = "current_folder_id", length = 26)
    private String currentFolderId; // Current section user is on

    @Column(columnDefinition = "JSON")
    private String completedFolderIds; // JSON array of completed folder IDs

    @Column(nullable = false)
    @Builder.Default
    private Double completionPercentage = 0.0;

    @Column
    private LocalDateTime lastAccessedAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    // Index for efficient queries
    @Table(indexes = {
            @Index(name = "idx_user_package", columnList = "userId, package_id", unique = true),
            @Index(name = "idx_package_id", columnList = "package_id"),
            @Index(name = "idx_user_id", columnList = "userId")
    })
    private static class IndexDefinition {
    }
}
