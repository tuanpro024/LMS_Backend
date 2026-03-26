package com.lms.pronunciation.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.pronunciation.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pronunciation_item_study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "study_set_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PronunciationItemStudySetProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 50)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(nullable = false)
    private Integer learnedItems;

    @Column(nullable = false)
    private Integer totalItems;

    private Double progressPercentage;

    private Instant firstStartedAt;

    private Instant completedAt;

    public void updateProgressPercent() {
        if (totalItems != null && totalItems > 0) {
            this.progressPercentage = (double) learnedItems / totalItems;
        } else {
            this.progressPercentage = 0.0;
        }
    }
}
