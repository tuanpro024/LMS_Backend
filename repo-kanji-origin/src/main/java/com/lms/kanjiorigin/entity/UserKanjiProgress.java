package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.kanjiorigin.entity.enums.KanjiStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_kanji_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "kanji_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKanjiProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_id", nullable = false)
    private KanjiOrigin kanjiOrigin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private KanjiStatus status = KanjiStatus.NOT_LEARNED;

    @Column(name = "review_count")
    @Builder.Default
    private int reviewCount = 0;

    @Column(name = "last_reviewed_at")
    private java.time.Instant lastReviewedAt;
}
