package com.lms.aipractice.dto.response;

import com.lms.aipractice.entity.enums.AttemptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AttemptResponse {
    private String id;
    private String userId;
    private String studySetId;
    private AttemptStatus status;
    private Instant startedAt;
    private Instant submittedAt;
    private Double totalScore;
    private Double maxScore;
    private Double progressPercent;
    private Instant createdAt;
}
