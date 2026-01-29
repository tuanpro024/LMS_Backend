package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "learning_events",
        indexes = {
                @Index(name = "idx_user_event_time", columnList = "user_id, occurred_at"),
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_module", columnList = "module_id"),
                @Index(name = "idx_study_set", columnList = "study_set_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningEvent extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    // References (nullable - tùy loại event)
    @Column(name = "package_id", length = 26)
    private String packageId;

    @Column(name = "folder_id", length = 26)
    private String folderId;

    @Column(name = "study_set_id", length = 26)
    private String studySetId;

    @Column(name = "module_id", length = 26)
    private String moduleId;

    @Column(name = "quest_id", length = 26)
    private String questId;

    // Event data
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    private Integer score;

    @Column(name = "exp_earned")
    private Integer expEarned;

    @Column(columnDefinition = "TEXT")
    private String metadata;  // JSON metadata

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();
}