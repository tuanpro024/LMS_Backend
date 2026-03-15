package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a study set in the Syllabus tree, belonging to a SyllabusFolder.
 */
@Entity
@Table(name = "syllabus_study_sets", indexes = {
        @Index(name = "idx_syllabus_studyset_folder", columnList = "syllabus_folder_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusStudySet extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "syllabus_folder_id", nullable = false, length = 26)
    private String syllabusFolderId;


}
