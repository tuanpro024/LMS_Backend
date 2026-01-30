package com.lms.learningpath.dto.request;

import com.lms.learningpath.entity.enums.QuestType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestRequest {

    @NotNull(message = "Quest type is required")
    private QuestType type;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String icon;

    @NotBlank(message = "Condition JSON is required")
    private String conditionJson;

    @Min(value = 0, message = "Reward exp must be >= 0")
    @Builder.Default
    private Integer rewardExp = 0;

    private String rewardBadgeId;

    private Instant startDate;

    private Instant endDate;

    @Builder.Default
    private Boolean isActive = true;
}