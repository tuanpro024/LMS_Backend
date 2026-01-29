package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.SetStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_set_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "study_set_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SetStatus status = SetStatus.LOCKED;

    @Column(name = "completed_modules")
    @Builder.Default
    private Integer completedModules = 0;

    @Column(name = "total_modules")
    @Builder.Default
    private Integer totalModules = 0;

    @Column(name = "best_score")
    private Integer bestScore;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "earned_exp")
    @Builder.Default
    private Integer earnedExp = 0;

    @Column(name = "last_studied_at")
    private Instant lastStudiedAt;
}