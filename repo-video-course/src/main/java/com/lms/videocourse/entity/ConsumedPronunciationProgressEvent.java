package com.lms.videocourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "consumed_pronunciation_progress_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedPronunciationProgressEvent {

    @Id
    @Column(length = 36)
    private String eventId;

    @Column(nullable = false, length = 26) // Will keep as 26 or adjust if length errors occur
    private String userId;

    @Column(nullable = false, length = 50) // Pronunciation uses 50 for studySetId in its DB. Safe to use 50 here.
    private String studySetId;

    @Column(nullable = false)
    private Instant consumedAt;
}
