package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.flashcard.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_card_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.NOT_LEARNED;

    @Column(name = "last_reviewed_at")
    private java.time.Instant lastReviewedAt;

    @Column(name = "review_count")
    @Builder.Default
    private int reviewCount = 0;
}
