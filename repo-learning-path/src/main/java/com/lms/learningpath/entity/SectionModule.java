package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a module/activity within a learning section.
 * This is a reference to a StudySet from another module (flashcard,
 * kanji-origin, etc.)
 * 
 * Structure: LearningPath -> LearningSection -> SectionModule (references
 * external StudySet)
 */
@Entity
@Table(name = "section_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionModule extends BaseEntity {

    @Column(name = "folder_id", nullable = false, length = 26)
    private String folderId; // Reference to Folder (LearningSection)

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private com.lms.learningpath.entity.enums.ModuleType moduleType;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId; // Reference to StudySet in external module

    @Column(nullable = false)
    private Integer moduleOrder; // Order within the section

    @Column(length = 255)
    private String displayTitle; // Optional: Override title for display

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Whether this module is required for section completion

    @Column(columnDefinition = "TEXT")
    private String moduleDescription; // Optional: Additional description

    // Index for efficient queries
    @Table(indexes = {
            @Index(name = "idx_folder_id", columnList = "folder_id"),
            @Index(name = "idx_module_order", columnList = "folder_id, moduleOrder")
    })
    private static class IndexDefinition {
    }
}
