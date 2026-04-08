package com.lms.identity.dto.response.ticket;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TicketAssigneeOptionResponse {
    private String id;
    private String email;
    private String fullName;
    private Set<String> roles;
}
