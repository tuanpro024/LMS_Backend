package com.lms.learningpath.entity.enums;

public enum EventType {
    // ========== Navigation ==========
    PACKAGE_VIEWED,
    FOLDER_VIEWED,
    SET_VIEWED,
    MODULE_VIEWED,

    // ========== Learning ==========
    MODULE_STARTED,
    MODULE_COMPLETED,
    MODULE_FAILED,
    MODULE_SKIPPED,

    // ========== Set & Package ==========
    SET_STARTED,
    SET_COMPLETED,
    SET_UNLOCKED,

    // ========== Assessment ==========
    QUIZ_STARTED,
    QUIZ_SUBMITTED,
    BOSS_TEST_STARTED,
    BOSS_TEST_COMPLETED,

    // ========== Gamification ==========
    QUEST_COMPLETED,
    QUEST_CLAIMED,
    LEVEL_UP,
    BADGE_EARNED,

    // ========== User Behavior ==========
    DAILY_STREAK,
    SESSION_START,
    SESSION_END
}