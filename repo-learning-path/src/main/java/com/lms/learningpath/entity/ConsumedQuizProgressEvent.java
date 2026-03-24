package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "consumed_quiz_progress_events", uniqueConstraints = @UniqueConstraint(columnNames = { "event_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedQuizProgressEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Column(nullable = false)
    private Instant consumedAt;
}
