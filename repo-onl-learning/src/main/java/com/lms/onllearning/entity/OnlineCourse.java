package com.lms.onllearning.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Bảng khóa học online hiển thị trên giao diện hệ thống.
 * syllabusId tham chiếu đến bảng syllabus (mirror từ CMS).
 * rating lưu số sao trung bình (0-5) do người dùng đánh giá.
 */
@Entity
@Table(name = "online_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineCourse extends BaseEntity {

    @Column(length = 50)
    private String code;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "course_type", length = 20)
    private String courseType;

    @Column(length = 20)
    private String level;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    /** URL ảnh thumbnail */
    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** FK trỏ tới syllabus.id (CMS mirror) */
    @Column(name = "syllabus_id", length = 26)
    private String syllabusId;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column
    private Double rating;

    @Column(name = "cms_synced", nullable = false)
    private boolean cmsSynced;

    public double getRating() {
        return rating == null ? 5.0 : rating;
    }
}
