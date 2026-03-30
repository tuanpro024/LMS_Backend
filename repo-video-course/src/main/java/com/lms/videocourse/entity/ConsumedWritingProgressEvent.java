package com.lms.videocourse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "consumed_writing_progress_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ConsumedWritingProgressEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String studySetId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant consumedAt;
}
