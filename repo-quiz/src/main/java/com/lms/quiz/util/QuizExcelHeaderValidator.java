package com.lms.quiz.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralized validation for Quiz Excel headers.
 */
public final class QuizExcelHeaderValidator {

    private QuizExcelHeaderValidator() {
    }

    public static void validateRequiredHeaders(Map<String, Integer> headerIndexMap, List<String> requiredHeaders) {
        Set<String> missingHeaders = new LinkedHashSet<>();
        for (String requiredHeader : requiredHeaders) {
            if (!headerIndexMap.containsKey(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }

        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException("Missing required Excel headers: " + String.join(", ", missingHeaders));
        }
    }
}
