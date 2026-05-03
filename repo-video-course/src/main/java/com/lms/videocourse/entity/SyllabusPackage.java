package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a package in the Syllabus tree.
 * Syllabus serves as a template/reference for creating actual courses.
 */
@Entity
@Table(name = "syllabus_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusPackage extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    // --- CMS sync fields ---

    @Column(name = "cms_syllabus_id", length = 50)
    private String cmsSyllabusId;

    @Column(name = "cms_course_id", length = 50)
    private String cmsCourseId;

    @Column(name = "course_name", length = 255)
    private String courseName;

    @Column(name = "course_description", columnDefinition = "TEXT")
    private String courseDescription;

    @Column(name = "course_price", precision = 12, scale = 2)
    private BigDecimal coursePrice;

    @Column(name = "course_type", length = 50)
    private String courseType;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "internal_package_id", length = 26)
    private String internalPackageId;
}
