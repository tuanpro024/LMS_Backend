package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a folder in the Syllabus tree, belonging to a SyllabusPackage.
 */
@Entity
@Table(name = "syllabus_folders", indexes = {
        @Index(name = "idx_syllabus_folder_package", columnList = "syllabus_package_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusFolder extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "syllabus_package_id", nullable = false, length = 26)
    private String syllabusPackageId;
}
