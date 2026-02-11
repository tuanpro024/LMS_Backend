package com.lms.multimedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetActiveSubtitleRequest {

    @NotBlank(message = "Subtitle ID is required")
    private String subtitleId;
}
