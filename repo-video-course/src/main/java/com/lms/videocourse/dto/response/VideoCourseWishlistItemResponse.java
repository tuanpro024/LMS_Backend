package com.lms.videocourse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCourseWishlistItemResponse {
    private String id;
    private String packageId;
    private String name;
    private String description;
    private String type;
    private String category;
    private String thumbnail;
    private BigDecimal price;
    private Instant addedAt;
}
