package com.lms.learningpath.entity;

import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a learning path within a StudySet.
 * For example: HSK1, HSK2, HSK3 are learning paths within the "HSK" StudySet.
 * Each learning path contains sequential steps that must be completed in order.
 */
@Entity
@Table(name = "learning_paths", indexes = {
        @Index(name = "idx_study_set", columnList = "study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseContentItem {

    // studySetId removed - now inherited via studySet relationship from
    // BaseContentItem
    // contentIndex inherited from BaseContentItem (nullable, for optional ordering)

    @Column(nullable = false, length = 255)
    private String title; // "HSK 1", "HSK 2", "HSK 3"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    private Integer estimatedHours; // Estimated time to complete

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, length = 26)
    private String createdBy; // User ID of creator (Admin/Teacher)

}
