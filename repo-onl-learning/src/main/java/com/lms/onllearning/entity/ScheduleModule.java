package com.lms.onllearning.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.onllearning.entity.enums.ScheduleModuleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Module ôn luyện gắn vào một buổi học (SyllabusSchedule).
 * Tương đương StepModule trong repo-learning-path.
 * Liên kết đến StudySet ở các repo ngoài (flashcard, writing, kanji, pronunciation, quiz).
 */
@Entity
@Table(name = "schedule_modules", indexes = {
        @Index(name = "idx_sm_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_sm_schedule_order", columnList = "schedule_id, module_order"),
        @Index(name = "idx_sm_deleted", columnList = "deleted")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_order", columnNames = {"schedule_id", "module_order"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleModule extends BaseEntity {

    @Column(name = "schedule_id", nullable = false, length = 26)
    private String scheduleId; // FK → syllabus_schedule.id

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 20)
    private ScheduleModuleType moduleType;

    @Column(name = "module_order", nullable = false)
    private Integer moduleOrder;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * ID của StudySet trong repo ngoài (flashcard, writing, kanji, pronunciation, quiz).
     * Null khi mới import và chờ xử lý.
     */
    @Column(name = "content_set_id", length = 26)
    private String contentSetId;

    @Column(name = "content_folder_id", length = 26)
    private String contentFolderId;

    /** JSON metadata tuỳ chọn (tên repo, link deeplink, ...). */
    @Column(name = "external_ref_json", columnDefinition = "TEXT")
    private String externalRefJson;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
