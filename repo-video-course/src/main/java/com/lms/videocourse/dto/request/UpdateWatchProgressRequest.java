package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWatchProgressRequest {

    @NotNull(message = "watchedSeconds is required")
    @Min(value = 0, message = "watchedSeconds must be >= 0")
    private Integer watchedSeconds; // Total seconds watched so far

    @Min(value = 0, message = "lastPositionSeconds must be >= 0")
    private Integer lastPositionSeconds; // Current playback position for resume
}
