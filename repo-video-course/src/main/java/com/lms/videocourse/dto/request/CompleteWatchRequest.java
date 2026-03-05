package com.lms.videocourse.dto.request;

import lombok.Data;

@Data
public class CompleteWatchRequest {
    /**
     * Force-complete the module regardless of watch percentage.
     * Admin/Teacher use only, or for cases where video tracking is unreliable.
     */
    private Boolean forceComplete = true;
}
