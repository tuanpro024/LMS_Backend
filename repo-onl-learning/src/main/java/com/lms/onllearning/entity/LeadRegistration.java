package com.lms.onllearning.entity;

import com.lms.onllearning.entity.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Lưu thông tin đăng ký tư vấn khóa học online.
 * userId lấy từ JWT ở backend, không nhận từ client.
 * UNIQUE KEY (user_id, course_code) đảm bảo idempotency.
 */
@Entity
@Table(name = "lead_registration", uniqueConstraints = @UniqueConstraint(name = "uk_user_course_code", columnNames = {
        "user_id", "course_code" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRegistration {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    /** Mã khóa học */
    @Column(name = "course_code", length = 50, nullable = false)
    private String courseCode;

    /** Snapshot tên khóa học tại thời điểm đăng ký */
    @Column(name = "course_name", length = 255, nullable = false)
    private String courseName;

    /** Loại khóa học (group, 1-1, ...) tại thời điểm đăng ký */
    @Column(name = "course_type", length = 20)
    private String courseType;

    /** userId từ JWT (không nhận từ client) */
    @Column(name = "user_id", length = 26)
    private String userId;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    /**
     * Trạng thái đăng ký:
     * PENDING_SALES – đang chờ tư vấn xác nhận ngoài hệ thống,
     * APPROVED      – đã được kích hoạt quyền học,
     * REJECTED      – đã bị từ chối.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_SALES;

    /** Thời điểm kích hoạt quyền học (null nếu chưa APPROVED) */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** userId của Admin/Manager đã kích hoạt (null nếu chưa APPROVED) */
    @Column(name = "approved_by", length = 26)
    private String approvedBy;
}
