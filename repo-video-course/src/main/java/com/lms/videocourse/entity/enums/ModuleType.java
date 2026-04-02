package com.lms.videocourse.entity.enums;

/**
 * Type of learning module that can be referenced from external repositories.
 * Used in AvailableModuleResponse to indicate the source of a study set.
 */
public enum ModuleType {
    FLASHCARD,
    WRITING,
    KANJI_ORIGIN,
    QUIZ,
    LISTENING,
    PRONUNCIATION,
    LEARNING_PATH
}
