package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a step within a learning path.
 * Steps are completed sequentially - must complete previous step to unlock
 * next.
 * For example: "Từ vựng cơ bản", "Ngữ pháp nâng cao", "Luyện tập tổng hợp"
 */
@Entity
@Table(name = "steps", indexes = {
        @Index(name = "idx_learning_path", columnList = "learning_path_id"),
        @Index(name = "idx_learning_path_order", columnList = "learning_path_id, step_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Step extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String learningPathId; // FK to LearningPath

    @Column(nullable = false, length = 255)
    private String title; // "Từ vựng cơ bản", "Ngữ pháp HSK1"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer stepOrder; // Sequential order (1, 2, 3, ...)

    @Column(length = 255)
    private String icon; // Icon URL or emoji

    @Column(length = 20)
    private String color; // Background color hex (#FF5733)

    private Integer estimatedMinutes; // Estimated time to complete this step

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Must be completed to progress?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
