package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCourseWishlistToggleResponse {
    private String packageId;
    private boolean inWishlist;
    private Instant addedAt;
}
