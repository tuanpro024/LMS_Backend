package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.videocourse.entity.enums.SyncStatus;
import com.lms.videocourse.entity.enums.SyncTriggerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Audit log for each syllabus sync run.
 * Tracks when a sync was triggered, its status, and any errors.
 */
@Entity
@Table(name = "syllabus_sync_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusSyncRun extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private SyncTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
