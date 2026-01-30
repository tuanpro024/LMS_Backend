package com.lms.learningpath.entity.enums;

/**
 * Enum representing different module types in the LMS system.
 * Each type corresponds to a microservice that manages StudySets.
 */
public enum ModuleType {
    FLASHCARD("repo-flashcard", "Flashcard"),
    KANJI_ORIGIN("repo-kanji-origin", "Kanji Origin"),
    WRITING("repo-writing", "Writing"),
    DICTIONARY("repo-dictionary", "Dictionary"),
    PRONUNCIATION("repo-pronunciation", "Pronunciation");

    private final String serviceName;
    private final String displayName;

    ModuleType(String serviceName, String displayName) {
        this.serviceName = serviceName;
        this.displayName = displayName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
