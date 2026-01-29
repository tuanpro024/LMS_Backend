package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.QuestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_quest_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "quest_id", "period"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuestProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private QuestDefinition quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QuestStatus status = QuestStatus.ACTIVE;

    @Column(name = "current_value")
    @Builder.Default
    private Integer currentValue = 0;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(length = 50)
    private String period;  // "2026-01-29" (daily), "2026-W05" (weekly), "2026-01" (monthly)

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "progress_json", columnDefinition = "TEXT")
    private String progressJson;
}