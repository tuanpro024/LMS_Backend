package com.lms.learningpath.util;

/**
 * Utility class for consistent string normalization.
 * Used to ensure consistent comparison of studySetNames and other text fields.
 */
public class StringNormalizer {

    /**
     * Normalize a string for consistent comparison.
     * - Trims leading/trailing whitespace
     * - Converts to lowercase
     * - Normalizes multiple consecutive spaces to single space
     * 
     * @param input String to normalize
     * @return Normalized string, or null if input is null
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    /**
     * Check if two strings are equal after normalization.
     * 
     * @param str1 First string
     * @param str2 Second string
     * @return true if normalized strings are equal, false otherwise
     */
    public static boolean equalsNormalized(String str1, String str2) {
        String normalized1 = normalize(str1);
        String normalized2 = normalize(str2);

        if (normalized1 == null && normalized2 == null) {
            return true;
        }
        if (normalized1 == null || normalized2 == null) {
            return false;
        }
        return normalized1.equals(normalized2);
    }
}
