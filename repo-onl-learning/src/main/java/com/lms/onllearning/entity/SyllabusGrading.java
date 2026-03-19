package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mirror bảng syllabus_grading từ CMS.
 * id là String(26) ULID từ CMS.
 */
@Entity
@Table(name = "syllabus_grading")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusGrading {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(length = 100)
    private String item;

    @Column(length = 50)
    private String type;

    @Column
    private Integer weight;

    @Column(length = 100)
    private String timing;

    @Column(length = 50)
    private String duration;

    @Column(length = 100)
    private String clo;

    @Column(name = "organizational_form", columnDefinition = "TEXT")
    private String organizationalForm;

    @Column(columnDefinition = "TEXT")
    private String criteria;

    @Column(name = "content_scope", columnDefinition = "TEXT")
    private String contentScope;
}
