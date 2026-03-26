package com.lms.pronunciation.dto.response;

import com.lms.pronunciation.entity.enums.PronunciationLearningStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationItemProgressResponse {
    private String id;
    private String userId;
    private String pronunciationItemId;
    private PronunciationLearningStatus status;
    private Instant firstListenedAt;
    private Instant lastListenedAt;
    private int listenCount;
}
