package com.lms.notification.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.notification.entity.enums.RecipientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "manual_notification_recipient",
        indexes = {
                @Index(name = "idx_recipient_campaign", columnList = "campaignId"),
                @Index(name = "idx_recipient_status", columnList = "status")
        })
public class ManualNotificationRecipient extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String campaignId;

    @Column(nullable = false, length = 26)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecipientStatus status;

    private Instant sentAt;

    @Column(length = 1000)
    private String errorMessage;
}
