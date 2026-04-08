package com.lms.learningpath.entity.enums;

/**
 * Enum representing different module types in the LMS system.
 * Each type corresponds to a learning activity within a StudySet.
 */
public enum ModuleType {
    FLASHCARD, // Học flashcard
    KANJI, // Học nguồn gốc hán tự
    KANJI_ORIGIN, // Legacy alias for KANJI (backward compatibility)
    WRITING, // Luyện viết
    PRONUNCIATION, // Luyện phát âm từ repo-pronunciation
    QUIZ, // Quiz kiểm tra (như Duolingo)
    VIDEO, // Xem video bài giảng
    READING, // Đọc hiểu
    LISTENING, // Nghe hiểu
    SPEAKING, // Luyện nói
    GRAMMAR // Ngữ pháp
}
