package com.lms.videocourse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "consumed_listening_progress_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedListeningProgressEvent {
    @Id
    private String eventId;
    
    @Column(nullable = false)
    private Instant consumedAt;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String studySetId;
}
