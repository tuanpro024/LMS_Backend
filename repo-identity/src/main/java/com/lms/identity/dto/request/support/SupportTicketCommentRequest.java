package com.lms.identity.dto.request.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupportTicketCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 2000)
    private String content;
}
