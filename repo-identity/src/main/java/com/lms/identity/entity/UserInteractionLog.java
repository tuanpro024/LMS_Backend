package com.lms.identity.entity;

import com.lms.common.dto.InteractionEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity for persisting user interaction logs to the database.
 * Only student interactions are stored.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_interaction_log", indexes = {
        @Index(name = "idx_interaction_user_id", columnList = "userId"),
        @Index(name = "idx_interaction_event_type", columnList = "eventType"),
        @Index(name = "idx_interaction_timestamp", columnList = "timestamp"),
        @Index(name = "idx_interaction_module", columnList = "module")
})
public class UserInteractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Column(length = 20)
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InteractionEventType eventType;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 50)
    private String module;

    private String targetId;

    @Column(length = 512)
    private String pageUrl;

    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
