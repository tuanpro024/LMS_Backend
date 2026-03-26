package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "consumed_writing_progress_events", uniqueConstraints = @UniqueConstraint(columnNames = { "event_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumedWritingProgressEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Column(nullable = false)
    private Instant consumedAt;
}