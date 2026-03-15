package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a specific step/lesson in the Syllabus tree, belonging to a SyllabusStudySet.
 */
@Entity
@Table(name = "syllabus_steps", indexes = {
        @Index(name = "idx_syllabus_step_studyset", columnList = "syllabus_study_set_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusStep extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_name", length = 255)
    private String moduleName;

    @Column(name = "syllabus_study_set_id", nullable = false, length = 26)
    private String syllabusStudySetId;
}
