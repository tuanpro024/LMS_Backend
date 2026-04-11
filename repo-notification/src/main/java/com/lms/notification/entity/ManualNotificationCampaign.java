package com.lms.notification.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.notification.entity.enums.AudienceType;
import com.lms.notification.entity.enums.CampaignStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "manual_notification_campaign")
public class ManualNotificationCampaign extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AudienceType audienceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CampaignStatus status;

    @Column(length = 26)
    private String createdBy;

}
