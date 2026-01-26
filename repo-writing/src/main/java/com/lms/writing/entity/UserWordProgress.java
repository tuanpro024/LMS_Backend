package com.lms.writing.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.content.common.entity.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_word_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "word_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWordProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentStatus status = ContentStatus.NOT_LEARNED;

    @Column(name = "last_reviewed_at")
    private java.time.Instant lastReviewedAt;

    @Column(name = "review_count")
    @Builder.Default
    private int reviewCount = 0;
}
