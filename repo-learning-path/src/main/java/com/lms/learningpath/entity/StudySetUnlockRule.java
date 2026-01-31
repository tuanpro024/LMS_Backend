package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Defines unlock rules for StudySets.
 * A StudySet can be unlocked when:
 * - Previous StudySet(s) are completed
 * - Specific conditions are met
 */
@Entity
@Table(name = "study_set_unlock_rules", indexes = @Index(name = "idx_study_set", columnList = "study_set_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetUnlockRule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String studySetId; // StudySet cần unlock

    @Column(length = 26)
    private String requiredStudySetId; // StudySet phải hoàn thành trước (null = không yêu cầu)

    @Column(nullable = false)
    @Builder.Default
    private Boolean requirePreviousInFolder = true; // Phải hoàn thành StudySet trước trong cùng Folder?

    @Column(nullable = false)
    @Builder.Default
    private Boolean requireAllRequiredModules = true; // Phải hoàn thành tất cả module bắt buộc?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
