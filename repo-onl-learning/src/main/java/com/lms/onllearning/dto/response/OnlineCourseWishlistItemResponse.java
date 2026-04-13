package com.lms.onllearning.dto.response;

import java.time.Instant;

public record OnlineCourseWishlistItemResponse(
        String id,
        String courseId,
        Instant addedAt,
        OnlineCourseResponse course) {
}
