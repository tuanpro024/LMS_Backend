package com.lms.videocourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "consumed_kanji_progress_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedKanjiProgressEvent {

    @Id
    @Column(length = 36, nullable = false)
    private String eventId;

    @Column(nullable = false, length = 150)
    private String userId;

    @Column(nullable = false, length = 36)
    private String studySetId;

    @Column(nullable = false)
    private Instant consumedAt;
}
