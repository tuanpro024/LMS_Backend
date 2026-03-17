package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mirror bảng syllabus_schedule từ CMS.
 * id là String(26) ULID từ CMS.
 */
@Entity
@Table(name = "syllabus_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusSchedule {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @Column(name = "session_no")
    private Integer sessionNo;

    @Column(length = 255)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String delivery;

    @Column(name = "session_lo", columnDefinition = "TEXT")
    private String sessionLo;

    @Column(name = "core_clo", length = 100)
    private String coreClo;

    @Column(name = "supporting_clo", length = 100)
    private String supportingClo;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Column(length = 10)
    private String itu;

    @Column(name = "student_materials", columnDefinition = "TEXT")
    private String studentMaterials;

    @Column(name = "teacher_materials", columnDefinition = "TEXT")
    private String teacherMaterials;

    @Column(name = "student_tasks", columnDefinition = "TEXT")
    private String studentTasks;

    @Column(name = "teacher_tasks", columnDefinition = "TEXT")
    private String teacherTasks;

    @Column(name = "student_materials_link", length = 500)
    private String studentMaterialsLink;

    @Column(name = "teacher_materials_link", length = 500)
    private String teacherMaterialsLink;

    @Column(name = "module_on_luyen", length = 500)
    private String moduleOnLuyen;
}
