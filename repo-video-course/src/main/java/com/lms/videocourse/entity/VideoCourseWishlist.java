package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "video_course_wishlist", uniqueConstraints = {
        @UniqueConstraint(name = "uq_video_wishlist_user_package", columnNames = { "user_id", "package_id" })
}, indexes = {
        @Index(name = "idx_video_wishlist_user", columnList = "user_id"),
        @Index(name = "idx_video_wishlist_package", columnList = "package_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCourseWishlist extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "package_id", nullable = false, length = 26)
    private String packageId;

    @Column(name = "package_name", nullable = false, length = 255)
    private String packageName;

    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @Column(name = "package_category", length = 100)
    private String packageCategory;

    @Column(name = "package_thumbnail", columnDefinition = "TEXT")
    private String packageThumbnail;

    @Column(name = "package_price", precision = 12, scale = 2)
    private BigDecimal packagePrice;
}
