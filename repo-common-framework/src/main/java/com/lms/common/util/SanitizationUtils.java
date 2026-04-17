package com.lms.common.util;

import org.springframework.util.StringUtils;

/**
 * Utility class for common data sanitization tasks to prevent injection attacks (like XSS).
 */
public class SanitizationUtils {

    private SanitizationUtils() {
        // Prevent instantiation
    }

    /**
     * Removes HTML tags from a string using a simple regular expression.
     * This is useful for plain text fields where NO HTML is allowed.
     * 
     * @param input The string to sanitize.
     * @return The sanitized string, or null if input is null.
     */
    public static String stripHtmlTags(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        // Basic regex to remove anything between < and >
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
