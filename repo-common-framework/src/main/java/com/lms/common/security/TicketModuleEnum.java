package com.lms.common.security;

/**
 * Enum module dùng trong @RequiresTicket annotation.
 * Phải khớp với TicketModule enum trong repo-identity.
 */
public enum TicketModuleEnum {
    FLASHCARD,
    QUIZ,
    AI,
    LISTENING_PRACTICE,
    MULTIMEDIA,
    DICTIONARY,
    KANJI_ORIGIN,
    LEARNING_PATH,
    VIDEO_COURSE,
    WRITING,
    PRONUNCIATION,
    OTHER
}
