package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.flashcard.entity.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "flashcard_study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "study_set_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardStudySetProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    @Column(name = "learned_cards", nullable = false)
    @Builder.Default
    private Integer learnedCards = 0;

    @Column(name = "total_cards", nullable = false)
    @Builder.Default
    private Integer totalCards = 0;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Column(name = "first_started_at")
    private Instant firstStartedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public void updateProgressPercent() {
        if (totalCards != null && totalCards > 0) {
            this.progressPercentage = (double) learnedCards / totalCards;
        } else {
            this.progressPercentage = 0.0;
        }
    }
}
