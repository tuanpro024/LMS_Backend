package com.lms.pronunciation.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.pronunciation.entity.enums.PronunciationLearningStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_pronunciation_item_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "pronunciation_item_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPronunciationItemProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pronunciation_item_id", nullable = false)
    private PronunciationItem pronunciationItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PronunciationLearningStatus status = PronunciationLearningStatus.NOT_LEARNED;

    private Instant firstListenedAt;

    private Instant lastListenedAt;

    @Builder.Default
    private int listenCount = 0;
}
