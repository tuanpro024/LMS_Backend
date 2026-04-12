package com.lms.identity.dto.response.support;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SupportTicketCommentResponse {
    private String id;
    private String ticketId;
    private String content;
    private String createdBy;
    private String creatorName;
    private String creatorAvatarUrl;
    private Instant createdAt;
}
