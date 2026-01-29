package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.learningpath.entity.enums.ModuleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningModule extends BaseEntity {

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ModuleType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String subtitle;

    private String icon;

    @Column(length = 20)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "content_set_id", length = 26)
    private String contentSetId;

    @Column(name = "external_ref_json", columnDefinition = "TEXT")
    private String externalRefJson;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;
}