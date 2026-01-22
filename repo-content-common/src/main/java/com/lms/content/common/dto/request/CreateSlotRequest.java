package com.lms.content.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSlotRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String slotNumber;

    private String description;

    @NotBlank(message = "Subject ID is required")
    private String subjectId;
}
