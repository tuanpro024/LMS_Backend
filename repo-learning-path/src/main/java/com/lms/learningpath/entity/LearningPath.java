package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a learning path within a StudySet.
 * For example: HSK1, HSK2, HSK3 are learning paths within the "HSK" StudySet.
 * Each learning path contains sequential steps that must be completed in order.
 */
@Entity
@Table(name = "learning_paths", indexes = {
        @Index(name = "idx_study_set", columnList = "study_set_id"),
        @Index(name = "idx_study_set_order", columnList = "study_set_id, display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String studySetId; // FK to StudySet (Content Common)

    @Column(nullable = false, length = 255)
    private String title; // "HSK 1", "HSK 2", "HSK 3"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @Column(nullable = false)
    private Integer displayOrder; // Order within the StudySet (1, 2, 3, ...)

    private Integer estimatedHours; // Estimated time to complete

    @Column(length = 20)
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, length = 26)
    private String createdBy; // User ID of creator (Admin/Teacher)

    // Helper method
    public String getLevelDisplay() {
        return level != null ? level.toUpperCase() : "BEGINNER";
    }
}
