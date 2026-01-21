package com.lms.writing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSlotRequest {

    @NotNull(message = "Subject ID is required")
    private String subjectId;

    @NotBlank(message = "Slot name is required")
    private String name;

    private String slotNumber;

    private String description;
}
