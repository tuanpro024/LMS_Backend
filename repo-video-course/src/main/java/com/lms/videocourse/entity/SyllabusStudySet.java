package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a study set in the Syllabus tree, belonging to a SyllabusFolder.
 */
@Entity
@Table(name = "syllabus_study_sets", indexes = {
        @Index(name = "idx_syllabus_studyset_folder", columnList = "syllabus_folder_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cms_unit_id", columnNames = "cms_unit_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusStudySet extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "syllabus_folder_id", nullable = false, length = 26)
    private String syllabusFolderId;

    // --- CMS sync fields ---

    @Column(name = "cms_unit_id", length = 50)
    private String cmsUnitId;

    @Column(name = "session_no")
    private Integer sessionNo;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
