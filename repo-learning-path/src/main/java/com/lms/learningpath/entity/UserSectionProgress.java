package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking user progress within a specific learning section (Folder).
 * Tracks which modules are completed and overall completion percentage.
 */
@Entity
@Table(name = "user_section_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSectionProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(name = "folder_id", nullable = false, length = 26)
    private String folderId; // Reference to Folder (LearningSection)

    @Column(columnDefinition = "JSON")
    private String completedModuleIds; // JSON array of completed module IDs

    @Column(nullable = false)
    @Builder.Default
    private Double completionPercentage = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime lastAccessedAt;

    // Index for efficient queries
    @Table(indexes = {
            @Index(name = "idx_user_folder", columnList = "userId, folder_id", unique = true),
            @Index(name = "idx_folder_id", columnList = "folder_id")
    })
    private static class IndexDefinition {
    }
}
