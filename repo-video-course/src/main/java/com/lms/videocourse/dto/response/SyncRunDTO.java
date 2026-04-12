package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for sync run audit history view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRunDTO {
    private String id;
    private String triggerType;
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    private String summary;
    private String errorMessage;
}
