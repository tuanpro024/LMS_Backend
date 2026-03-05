package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a step (chapter/section) within a VideoCourse.
 * Steps are completed sequentially — must complete previous step to unlock
 * next.
 * Mirrors Step from repo-learning-path.
 * Example: "Bài 1: Giới thiệu", "Bài 2: Ngữ pháp cơ bản"
 */
@Entity
@Table(name = "video_steps", indexes = {
        @Index(name = "idx_vs_course", columnList = "video_course_id"),
        @Index(name = "idx_vs_course_order", columnList = "video_course_id, step_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStep extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String videoCourseId; // FK to VideoCourse

    @Column(nullable = false, length = 255)
    private String title; // "Bài 1: Giới thiệu", "Chương 2: Ngữ pháp"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer stepOrder; // Sequential order (1, 2, 3, ...)

    @Column(length = 255)
    private String icon; // Icon URL or emoji

    @Column(length = 20)
    private String color; // Background color hex (#FF5733)

    private Integer estimatedMinutes; // Estimated time in minutes

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Must be completed to progress?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
