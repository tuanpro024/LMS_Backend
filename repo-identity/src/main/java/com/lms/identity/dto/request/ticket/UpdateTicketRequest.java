package com.lms.identity.dto.request.ticket;

import com.lms.identity.entity.ticket.TicketModule;
import lombok.Data;

@Data
public class UpdateTicketRequest {

    private String description;

    private TicketModule module;

    private String assignedId;
}
