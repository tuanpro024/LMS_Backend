package com.lms.learningpath.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for marking a module as completed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteModuleRequest {
    private String moduleId;
    private List<String> completedItemIds; // IDs of items (cards, words, etc.) completed
}
