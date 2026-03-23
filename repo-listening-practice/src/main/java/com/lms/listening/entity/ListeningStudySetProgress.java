package com.lms.listening.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.listening.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "listening_study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "study_set_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningStudySetProgress extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    private Integer completedVideos;

    @Column(nullable = false)
    private Integer totalVideos;

    private Double progressPercentage;

    private Instant firstStartedAt;
    
    private Instant completedAt;

    public void updateProgressPercent() {
        if (totalVideos != null && totalVideos > 0) {
            this.progressPercentage = (double) completedVideos / totalVideos;
        } else {
            this.progressPercentage = 0.0;
        }
    }
}
