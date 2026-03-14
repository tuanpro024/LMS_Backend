package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Lưu thông tin đăng ký tư vấn khóa học online.
 * userId lấy từ JWT ở backend, không nhận từ client.
 * UNIQUE KEY (user_id, syllabus_id) đảm bảo idempotency.
 */
@Entity
@Table(
    name = "lead_registration",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_syllabus",
        columnNames = {"user_id", "syllabus_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRegistration {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    /** ID của syllabus bên CMS */
    @Column(name = "syllabus_id", length = 50, nullable = false)
    private String syllabusId;

    /** Snapshot tên khóa học — tránh phụ thuộc CMS khi xem danh sách leads */
    @Column(name = "syllabus_name", length = 255, nullable = false)
    private String syllabusName;

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
}
