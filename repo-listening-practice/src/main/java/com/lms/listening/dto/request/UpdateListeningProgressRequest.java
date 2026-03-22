package com.lms.listening.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateListeningProgressRequest {
    @NotNull(message = "Watched seconds is required")
    private Integer watchedSeconds;

    private Integer lastPositionSeconds;
}
