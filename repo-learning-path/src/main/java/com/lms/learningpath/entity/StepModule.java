package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a practice module within a Step.
 * Links to external StudySets from other repos (flashcard, writing,
 * kanji-origin, pronunciation).
 * Admin/Teacher selects which external study sets to include in each step.
 */
@Entity
@Table(name = "step_modules", indexes = {
        @Index(name = "idx_step", columnList = "step_id"),
        @Index(name = "idx_step_order", columnList = "step_id, module_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepModule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String stepId; // FK to Step

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModuleType moduleType; // FLASHCARD, WRITING, KANJI_ORIGIN, PRONUNCIATION, etc.

    @Column(nullable = false)
    private Integer moduleOrder; // Display order within step (1, 2, 3, ...)

    @Column(nullable = false, length = 255)
    private String title; // "Luyện từ vựng HSK1", "Luyện viết chữ Hán"

    @Column(columnDefinition = "TEXT")
    private String description;

    // Reference to content in other modules
    @Column(length = 26)
    private String contentSetId; // ID của StudySet trong module khác (flashcard, kanji, etc.)

    @Column(length = 26)
    private String contentFolderId; // Optional: Folder trong module khác

    @Column(columnDefinition = "TEXT")
    private String externalRefJson; // JSON metadata cho module external

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Module bắt buộc?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
