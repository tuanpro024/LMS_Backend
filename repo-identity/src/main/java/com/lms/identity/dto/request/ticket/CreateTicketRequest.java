package com.lms.identity.dto.request.ticket;

import com.lms.identity.entity.ticket.TicketModule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Module is required")
    private TicketModule module;

    @NotBlank(message = "AssignedId is required")
    private String assignedId;
}
