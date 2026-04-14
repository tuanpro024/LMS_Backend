package com.lms.onllearning.entity;

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
@Table(name = "online_course_wishlist", uniqueConstraints = {
        @UniqueConstraint(name = "uq_onl_wishlist_user_course", columnNames = { "user_id", "course_id" })
}, indexes = {
        @Index(name = "idx_onl_wishlist_user", columnList = "user_id"),
        @Index(name = "idx_onl_wishlist_course", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineCourseWishlist extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "course_id", nullable = false, length = 26)
    private String courseId;
}
