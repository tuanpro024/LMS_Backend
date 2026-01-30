package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleCompleteResponse {

    private String moduleId;
    private String status;
    private Integer score;
    private Integer earnedExp;
    private Instant completedAt;

    // Set progress
    private SetProgressResponse setProgress;

    // Suggested next module
    private ModuleResponse suggestedNextModule;

    // Set completion
    private Boolean setCompleted;
    private SetCompletionResult setResult;

    // Unlocked sets
    private List<UnlockedSetInfo> unlockedSets;

    // Quests updated
    private List<QuestProgressInfo> questsUpdated;
}