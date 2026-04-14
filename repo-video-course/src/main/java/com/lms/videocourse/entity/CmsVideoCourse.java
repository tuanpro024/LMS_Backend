package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Stores the list of VIDEO courses fetched from the external CMS.
 * Each record maps 1:1 to a course returned by CMS courses/list API.
 */
@Entity
@Table(name = "cms_video_courses", indexes = {
        @Index(name = "idx_cms_vc_syllabus_id", columnList = "syllabus_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cms_course_id", columnNames = "cms_course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsVideoCourse extends BaseEntity {

    @Column(name = "cms_course_id", nullable = false, length = 50)
    private String cmsCourseId;

    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "course_type", length = 50)
    private String courseType;

    @Column(length = 50)
    private String level;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "syllabus_id", length = 50)
    private String syllabusId;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;
}
