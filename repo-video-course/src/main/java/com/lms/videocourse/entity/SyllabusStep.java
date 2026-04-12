package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a specific step/lesson in the Syllabus tree, belonging to a SyllabusStudySet.
 */
@Entity
@Table(name = "syllabus_steps", indexes = {
        @Index(name = "idx_syllabus_step_studyset", columnList = "syllabus_study_set_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cms_video_lesson_id", columnNames = "cms_video_lesson_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusStep extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** @deprecated Kept for backward compatibility. Use SyllabusActivity instead. */
    @Column(name = "module_name", length = 255)
    private String moduleName;

    @Column(name = "syllabus_study_set_id", nullable = false, length = 26)
    private String syllabusStudySetId;

    // --- CMS sync fields ---

    @Column(name = "cms_video_lesson_id", length = 50)
    private String cmsVideoLessonId;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
