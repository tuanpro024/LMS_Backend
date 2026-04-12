package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents the 5th level in the Syllabus tree: activities (modules) within a video lesson step.
 * Maps to CMS modules[] inside each video_lesson.
 */
@Entity
@Table(name = "syllabus_activities", indexes = {
        @Index(name = "idx_syllabus_activity_step", columnList = "syllabus_step_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cms_module_id", columnNames = "cms_module_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusActivity extends BaseEntity {

    @Column(name = "syllabus_step_id", nullable = false, length = 26)
    private String syllabusStepId;

    @Column(name = "cms_module_id", nullable = false, length = 50)
    private String cmsModuleId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_content", columnDefinition = "TEXT")
    private String videoContent;

    @Column(name = "document_content", columnDefinition = "TEXT")
    private String documentContent;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
