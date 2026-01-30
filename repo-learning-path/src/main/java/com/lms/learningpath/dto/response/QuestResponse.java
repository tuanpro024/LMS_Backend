package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.QuestStatus;
import com.lms.learningpath.entity.enums.QuestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestResponse {

    private String id;
    private QuestType type;
    private String title;
    private String description;
    private String icon;
    private Integer rewardExp;
    private String rewardBadgeId;

    // User progress (nếu có)
    private QuestStatus status;
    private Integer currentValue;
    private Integer targetValue;
    private String period;
    private Instant completedAt;
    private Instant claimedAt;
    private Instant expiresAt;

    private Instant createdAt;
}