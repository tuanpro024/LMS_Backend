package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a learning module within a StudySet.
 * Each module can be:
 * - FLASHCARD: Học flashcard từ repo-flashcard
 * - KANJI: Học Kanji từ repo-kanji-origin
 * - WRITING: Luyện viết từ repo-writing
 * - QUIZ: Quiz kiểm tra kiến thức (kiểu Duolingo)
 * - VIDEO: Xem video bài giảng
 * - READING: Đọc hiểu
 */
@Entity
@Table(name = "study_set_modules", indexes = {
        @Index(name = "idx_study_set", columnList = "study_set_id"),
        @Index(name = "idx_module_order", columnList = "study_set_id, module_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetModule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String studySetId; // FK to StudySet (Content Common)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModuleType moduleType;

    @Column(nullable = false)
    private Integer moduleOrder; // Thứ tự hiển thị (1, 2, 3,...)

    @Column(nullable = false, length = 255)
    private String title; // "Từ Vựng & Ngữ Pháp"

    @Column(columnDefinition = "TEXT")
    private String description;

    private String icon; // Icon URL hoặc emoji
    private String color; // Màu background (#FF5733)

    // Reference to content in other modules
    @Column(length = 26)
    private String contentSetId; // ID của StudySet trong module khác (flashcard, kanji, etc.)

    @Column(length = 26)
    private String contentFolderId; // Optional: Folder trong module khác

    @Column(columnDefinition = "TEXT")
    private String externalRefJson; // JSON metadata cho module external

    private Integer estimatedMinutes; // Thời gian ước tính

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Module bắt buộc?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
