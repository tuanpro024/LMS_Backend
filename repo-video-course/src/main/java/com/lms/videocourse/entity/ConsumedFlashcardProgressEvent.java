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
@Table(name = "consumed_flashcard_progress_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedFlashcardProgressEvent {

    @Id
    @Column(length = 36)
    private String eventId;

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 26)
    private String studySetId;

    @Column(nullable = false)
    private Instant consumedAt;
}
