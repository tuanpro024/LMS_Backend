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
public class VideoCourseReviewResponse {
    private String id;
    private String packageId;
    private String userId;
    private String displayName;
    private String avatarUrl;
    private int rating;
    private String feedback;
    private Instant createdAt;
    private Instant updatedAt;
}
