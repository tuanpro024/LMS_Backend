package com.lms.listening.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.listening.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "listening_video_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "study_set_id", "video_code" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningVideoProgress extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 26)
    private String studySetId;

    @Column(name = "video_code", nullable = false, length = 50)
    private String videoCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    private Integer watchedSeconds;

    @Column(nullable = false)
    private Integer totalDurationSeconds;

    @Column(nullable = false)
    private Double watchPercent;

    @Column(nullable = false)
    private Integer lastPositionSeconds;

    @Column(nullable = false)
    private Boolean isCompleted;

    private Instant firstStartedAt;
    
    private Instant lastWatchedAt;
    
    private Instant completedAt;

    public void updateWatchTime(int newWatchedSeconds) {
        if (this.totalDurationSeconds > 0) {
            this.watchedSeconds = Math.max(this.watchedSeconds, newWatchedSeconds);
            this.watchPercent = (double) this.watchedSeconds / this.totalDurationSeconds;
        } else {
            this.watchedSeconds = Math.max(this.watchedSeconds, newWatchedSeconds);
            this.watchPercent = 0.0;
        }
        this.lastWatchedAt = Instant.now();
    }

    public boolean qualifiesForCompletion() {
        return this.watchPercent >= 0.8 && !this.isCompleted && this.status != ProgressStatus.COMPLETED;
    }

    public void markCompleted() {
        this.isCompleted = true;
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = Instant.now();
    }
}
