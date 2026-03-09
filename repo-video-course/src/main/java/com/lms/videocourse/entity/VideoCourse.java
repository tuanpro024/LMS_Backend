package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a video course within a StudySet.
 * Uses a plain String studySetId (denormalized) rather than @ManyToOne
 * to keep this service independent of repo-content-common StudySet entity.
 * Mirrors LearningPath structure from repo-learning-path.
 */
@Entity
@Table(name = "video_courses", indexes = {
        @Index(name = "idx_vc_study_set", columnList = "study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCourse extends BaseEntity {

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Column(name = "content_index")
    private Integer contentIndex;

    @Column(nullable = false, length = 255)
    private String title; // "Khóa học N5", "Tiếng Nhật Sơ Cấp"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    private Integer estimatedHours; // Tổng giờ học ước tính

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, length = 26)
    private String createdBy; // User ID of creator (Admin/Teacher)
}
