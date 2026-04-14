package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Stores the mapping between a syllabus activity (from CMS) and a target practice module
 * in the internal learning system. Used for auto-generating courses.
 */
@Entity
@Table(name = "syllabus_activity_module_mappings", indexes = {
        @Index(name = "idx_samm_activity", columnList = "syllabus_activity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusActivityModuleMapping extends BaseEntity {

    @Column(name = "syllabus_activity_id", nullable = false, length = 26)
    private String syllabusActivityId;

    @Column(name = "target_module_type", nullable = false, length = 50)
    private String targetModuleType;

    @Column(name = "target_content_set_id", length = 50)
    private String targetContentSetId;

    @Column(name = "target_content_folder_id", length = 50)
    private String targetContentFolderId;

    @Column(name = "target_package_id", length = 50)
    private String targetPackageId;

    @Column(name = "target_module_title", length = 255)
    private String targetModuleTitle;

    @Column(name = "target_module_description", columnDefinition = "TEXT")
    private String targetModuleDescription;

    @Column(name = "is_required")
    private boolean isRequired;

    @Column(length = 20)
    private String status;

    @Column(name = "created_by", length = 50)
    private String createdBy;
}
