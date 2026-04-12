package com.lms.identity.dto.response.support;

import com.lms.identity.entity.support.SupportTicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SupportTicketResponse {
    private String id;
    private String title;
    private String description;
    private SupportTicketStatus status;
    private String createdBy;
    private String creatorName;
    private String creatorAvatarUrl;
    private String assignedTo;
    private Instant createdAt;
    private Instant updatedAt;
}
