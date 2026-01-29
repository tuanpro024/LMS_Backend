package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ModuleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_module_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModuleProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private LearningModule module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ModuleStatus status = ModuleStatus.NOT_STARTED;

    private Integer score;

    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
}