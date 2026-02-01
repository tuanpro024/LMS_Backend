package com.lms.content.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySetResponse {

    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private boolean isPrivate;
    private String userId;
    private String unlockRuleJson;
    private Integer estimatedMinutes;
    private int totalItems;
    private Instant createdAt;
    private Instant updatedAt;
}
