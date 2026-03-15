package com.lms.writing.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for character-related operations.
 * Used to convert Chinese word strings to/from JSON character arrays.
 */
public final class CharacterUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CharacterUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Convert a word string to a JSON array of individual characters.
     * Example: "你好" -> ["你", "好"]
     *
     * @param word the word to convert
     * @return JSON array string of characters
     */
    public static String wordToJsonArray(String word) {
        if (word == null || word.isEmpty()) {
            return "[]";
        }

        try {
            // Use codePoints() to properly handle supplementary Unicode characters (e.g.,
            // CJK Extension B)
            String[] charStrings = word.codePoints()
                    .mapToObj(cp -> new String(Character.toChars(cp)))
                    .toArray(String[]::new);
            return objectMapper.writeValueAsString(charStrings);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * Parse a JSON array of characters to a List of strings.
     * Example: ["你", "好"] -> List.of("你", "好")
     *
     * @param charactersJson the JSON array string
     * @return List of character strings
     */
    public static List<String> jsonArrayToList(String charactersJson) {
        if (charactersJson == null || charactersJson.isEmpty()) {
            return List.of();
        }

        try {
            String[] chars = objectMapper.readValue(charactersJson, String[].class);
            return Arrays.asList(chars);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
