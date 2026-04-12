package com.lms.notification.dto.response;

import com.lms.notification.entity.enums.AudienceType;
import com.lms.notification.entity.enums.CampaignStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CampaignResponse {
    private String id;
    private String title;
    private String message;
    private AudienceType audienceType;
    private CampaignStatus status;
    private Instant createdAt;
}
