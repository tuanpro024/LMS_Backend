package com.lms.content.common.dto.excel;

import com.lms.content.common.entity.StudySet;

/**
 * Interface for content items that can be imported (Card, Word, etc.)
 * This allows generic import logic to work with different content types
 * Note: Different implementations may map these to different fields
 * (e.g., term->term for Card, term->word for Word)
 */
public interface ImportableContentItem {

    /**
     * Set the study set this content item belongs to
     */
    void setStudySet(StudySet studySet);

    /**
     * Set the main term/word
     * For Card: maps to term field
     * For Word: maps to word field
     */
    default void setTerm(String term) {
        // Override in implementation if needed
    }

    /**
     * Set the definition/meaning
     * For Card: maps to definition field
     * For Word: maps to meaning field
     */
    default void setDefinition(String definition) {
        // Override in implementation if needed
    }

    /**
     * Set pinyin
     */
    default void setPinyin(String pinyin) {
        // Override in implementation if needed
    }

    /**
     * Set Sino-Vietnamese reading
     */
    default void setSinoVn(String sinoVn) {
        // Override in implementation if needed
    }

    /**
     * Set word type
     */
    default void setWordType(String wordType) {
        // Override in implementation if needed
    }

    /**
     * Set HSK level
     */
    default void setHskLevel(String hskLevel) {
        // Override in implementation if needed
    }

    /**
     * Set image URL for word
     */
    default void setImageWord(String imageWord) {
        // Override in implementation if needed
    }

    /**
     * Set Sino-Vietnamese origin explanation
     */
    default void setSinoOrigin(String sinoOrigin) {
        // Override in implementation if needed
    }

    /**
     * Set image URL for origin
     */
    default void setImageOrigin(String imageOrigin) {
        // Override in implementation if needed
    }

    /**
     * Set example sentence
     */
    default void setExampleSentence(String exampleSentence) {
        // Override in implementation if needed
    }

    /**
     * Set example pinyin
     */
    default void setExamplePinyin(String examplePinyin) {
        // Override in implementation if needed
    }

    /**
     * Set example meaning
     */
    default void setExampleMeaning(String exampleMeaning) {
        // Override in implementation if needed
    }

    /**
     * Set characters JSON
     */
    default void setCharacters(String charactersJson) {
        // Override in implementation if needed
    }
}
