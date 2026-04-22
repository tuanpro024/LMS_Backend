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

@Entity
@Table(name = "video_course_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_user_package", columnNames = { "user_id", "package_id" })
}, indexes = {
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_package", columnList = "package_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCourseReview extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "package_id", nullable = false, length = 26)
    private String packageId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}
