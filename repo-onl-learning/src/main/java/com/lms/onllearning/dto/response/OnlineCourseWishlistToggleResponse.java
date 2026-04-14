package com.lms.onllearning.dto.response;

import java.time.Instant;

public record OnlineCourseWishlistToggleResponse(
        String courseId,
        boolean inWishlist,
        Instant addedAt) {
}
