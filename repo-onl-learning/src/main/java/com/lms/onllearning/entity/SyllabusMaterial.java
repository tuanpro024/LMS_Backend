package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mirror bảng syllabus_material từ CMS.
 * id là Long (auto-increment do CMS quản lý).
 */
@Entity
@Table(name = "syllabus_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusMaterial {

    @Id
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(length = 255)
    private String title;

    @Column(length = 50)
    private String type;

    @Column(length = 50)
    private String isbn;

    @Column(length = 255)
    private String author;

    @Column(length = 255)
    private String publisher;

    @Column(length = 10)
    private String year;

    @Column(length = 100)
    private String edition;
}
