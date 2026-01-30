package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestProgressInfo {

    private String questId;
    private String title;
    private String status;
    private Integer currentValue;
    private Integer targetValue;
    private Integer rewardExp;
}