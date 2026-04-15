package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a folder in the Syllabus tree, belonging to a SyllabusPackage.
 */
@Entity
@Table(name = "syllabus_folders", indexes = {
        @Index(name = "idx_syllabus_folder_package", columnList = "syllabus_package_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cms_folder_id", columnNames = "cms_folder_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusFolder extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "syllabus_package_id", nullable = false, length = 26)
    private String syllabusPackageId;

    // --- CMS sync fields ---

    @Column(name = "cms_folder_id", length = 50)
    private String cmsFolderId;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
