package com.lms.listening.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddVideoRequest {

    @NotBlank(message = "Video code is required")
    private String videoCode; // Video code from multimedia service

    @Min(value = 0, message = "Display order must be non-negative")
    private Integer displayOrder;

    // Additional fields can be added if needed
}
