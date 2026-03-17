package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mirror bảng syllabus_clo từ CMS.
 * id là String(50) lấy từ CMS (ví dụ "CLO 1", "CLO 2"...).
 */
@Entity
@Table(name = "syllabus_clo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusClo {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
