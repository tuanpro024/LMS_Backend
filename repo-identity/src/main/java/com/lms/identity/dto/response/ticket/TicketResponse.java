package com.lms.identity.dto.response.ticket;

import com.lms.identity.entity.ticket.TicketModule;
import com.lms.identity.entity.ticket.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TicketResponse {

    private String id;
    private String description;
    private TicketModule module;
    private TicketStatus status;

    private String assignedId;
    private String assignedEmail;
    private String assignedFullName;

    private String createdBy;
    private String createdByEmail;

    private Instant createdAt;
    private Instant updatedAt;
}
