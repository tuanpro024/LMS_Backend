package com.lms.videocourse.dto.response;

import com.lms.content.common.dto.response.PackageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Extended Package DTO specific to video course views.
 * Wraps all PackageResponse fields + review summary.
 * This avoids modifying the shared PackageResponse in repo-content-common.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCoursePackageResponse {
    private String id;
    private String name;
    private String type;
    private String description;
    private String thumbnail;
    private String category;
    private BigDecimal price;
    private String pricingType;
    private String userId;
    private Integer enrollmentCount;
    private String publishStatus;
    private Instant createdAt;
    private Instant updatedAt;

    // Review summary fields
    private Double averageRating;
    private Integer reviewCount;

    /**
     * Build from a PackageResponse + review summary data.
     */
    public static VideoCoursePackageResponse from(PackageResponse pkg, Double avgRating, Integer reviewCount) {
        return VideoCoursePackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .type(pkg.getType() == null ? null : pkg.getType().name())
                .description(pkg.getDescription())
                .thumbnail(pkg.getThumbnail())
                .category(pkg.getCategory() == null ? null : pkg.getCategory().name())
                .price(pkg.getPrice())
                .pricingType(pkg.getPricingType())
                .userId(pkg.getUserId())
                .enrollmentCount(pkg.getEnrollmentCount())
                .publishStatus(pkg.getPublishStatus() == null ? null : pkg.getPublishStatus().name())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }

    /**
     * Build from a PackageResponse without review data (defaults to null).
     */
    public static VideoCoursePackageResponse from(PackageResponse pkg) {
        return from(pkg, null, null);
    }
}
