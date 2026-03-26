package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "video_practice_module_progresses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_module_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VideoPracticeModuleProgress extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String videoModuleId;

    @Column(nullable = false, length = 26)
    private String stepId;

    @Column(length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleType moduleType;

    @Column(nullable = false)
    private String contentSetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status;

    private double progressPercentage;

    private Instant firstStartedAt;
    
    private Instant completedAt;

    private String lastSyncedEventId;
    
    private Instant lastSyncedAt;
}
