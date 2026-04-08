package com.lms.onllearning.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "schedule_module_user_progress", indexes = {
        @Index(name = "idx_smup_module", columnList = "module_id"),
        @Index(name = "idx_smup_user", columnList = "user_id"),
        @Index(name = "idx_smup_deleted", columnList = "deleted")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_smup_module_user", columnNames = { "module_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleModuleUserProgress extends BaseEntity {

    @Column(name = "module_id", nullable = false, length = 26)
    private String moduleId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "progress_percentage", nullable = false)
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "completed_items")
    private Integer completedItems;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "last_interacted_at")
    private Instant lastInteractedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
