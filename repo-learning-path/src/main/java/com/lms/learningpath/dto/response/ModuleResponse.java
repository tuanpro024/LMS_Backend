package com.lms.learningpath.dto.response;

import com.lms.learningpath.entity.enums.ModuleStatus;
import com.lms.learningpath.entity.enums.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponse {

    private String id;
    private String studySetId;
    private Integer orderIndex;
    private ModuleType type;
    private String title;
    private String subtitle;
    private String icon;
    private String color;
    private String description;
    private String contentSetId;
    private String externalRefJson;
    private Integer estimatedMinutes;
    private Boolean isRequired;

    // User progress (nếu có)
    private ModuleStatus status;
    private Integer score;
    private Integer attempts;
    private Instant lastAttemptAt;
    private Instant completedAt;

    private Instant createdAt;
    private Instant updatedAt;
}