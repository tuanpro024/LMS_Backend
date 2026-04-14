package com.lms.identity.dto.request.support;

import com.lms.identity.entity.support.SupportTicketCategory;
import com.lms.identity.entity.support.SupportTicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SupportTicketCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 4000)
    private String description;

    @NotNull(message = "Category is required")
    private SupportTicketCategory category;

    @NotNull(message = "Priority is required")
    private SupportTicketPriority priority;

    private List<String> files;
}
