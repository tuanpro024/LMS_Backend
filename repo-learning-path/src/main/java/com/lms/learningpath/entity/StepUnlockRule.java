package com.lms.learningpath.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Defines unlock rules for Steps.
 * By default, a step requires the previous step to be completed.
 * Sequential unlocking: Step 1 → Step 2 → Step 3
 */
@Entity
@Table(name = "step_unlock_rules", indexes = {
        @Index(name = "idx_step", columnList = "step_id"),
        @Index(name = "idx_required_step", columnList = "required_step_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepUnlockRule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String stepId; // Step cần unlock

    @Column(length = 26)
    private String requiredStepId; // Step phải hoàn thành trước (null = step đầu tiên)

    @Column(nullable = false)
    @Builder.Default
    private Boolean requireAllModules = true; // Phải hoàn thành tất cả module bắt buộc?

    private Integer minimumScore; // Điểm tối thiểu (cho tương lai khi có quiz)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
