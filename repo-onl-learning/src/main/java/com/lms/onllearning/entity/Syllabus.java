package com.lms.onllearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Mirror bảng syllabus từ CMS.
 * Id lấy trực tiếp từ CMS (ULID 26 ký tự) — không tự sinh.
 */
@Entity
@Table(name = "syllabus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Syllabus {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Column(length = 50)
    private String code;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(name = "hsk_level", length = 20)
    private String hskLevel;

    @Column(length = 50)
    private String type;

    @Column(length = 20)
    private String version;

    @Column(length = 100)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "distribution_hours", columnDefinition = "TEXT")
    private String distributionHours;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(columnDefinition = "TEXT")
    private String prerequisite;

    @Column(name = "training_program", length = 100)
    private String trainingProgram;

    @Column(name = "student_responsibilities", columnDefinition = "TEXT")
    private String studentResponsibilities;

    @Column(name = "minimum_passing_score", length = 50)
    private String minimumPassingScore;

    @Column(name = "score_range", length = 255)
    private String scoreRange;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** JSON array of teaching method strings */
    @Column(name = "teaching_methods", columnDefinition = "JSON")
    private String teachingMethods;

    /** JSON array of learning tool strings */
    @Column(name = "learning_tools", columnDefinition = "JSON")
    private String learningTools;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('DRAFT','PENDING','APPROVED','REJECTED','ACTIVE') DEFAULT 'DRAFT'")
    @Builder.Default
    private SyllabusStatus status = SyllabusStatus.DRAFT;

    @Column(name = "is_visible", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    @Builder.Default
    private boolean isVisible = true;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "created_by", length = 26)
    private String createdBy;

    @Column(name = "approved_by", length = 26)
    private String approvedBy;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    @Builder.Default
    private boolean deleted = false;

    /** Quan hệ với các bảng con (cascade khi sync) */
    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SyllabusClo> cloList = new ArrayList<>();

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SyllabusMaterial> materials = new ArrayList<>();

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SyllabusSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SyllabusGrading> gradings = new ArrayList<>();

    public enum SyllabusStatus {
        DRAFT, PENDING, APPROVED, REJECTED, ACTIVE
    }
}
