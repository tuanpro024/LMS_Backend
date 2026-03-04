package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.videocourse.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks user progress on an entire VideoCourse.
 * Aggregates progress from all VideoSteps in the course.
 * Mirrors LearningPathProgress from repo-learning-path.
 */
@Entity
@Table(name = "video_course_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "video_course_id" }), indexes = {
                @Index(name = "idx_vcp_user_study_set", columnList = "user_id, study_set_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCourseProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String videoCourseId; // FK to VideoCourse

    @Column(nullable = false, length = 26)
    private String studySetId; // Denormalized from VideoCourse

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedSteps = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSteps = 0;

    @Column(length = 26)
    private String currentStepId; // Current step user is working on

    private Instant firstStartedAt;
    private Instant completedAt;

    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public double getProgressPercentage() {
        if (totalSteps == 0)
            return 0.0;
        return (completedSteps * 100.0) / totalSteps;
    }
}
