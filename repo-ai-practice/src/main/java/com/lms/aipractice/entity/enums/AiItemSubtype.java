package com.lms.aipractice.entity.enums;

/**
 * Maps 1:1 to AI_LMS QuestionType / SpeakingPartType.
 * Prefix determines which AI provider to use:
 * - "speaking_*" → HSK_API /api/v2/speaking/grade
 * - "audio_*" → HSK_API /api/v2/audio/compare
 * - others → HSK_API /api/v2/grade
 */
public enum AiItemSubtype {
    // Writing subtypes → HSK_API /api/v2/grade
    SENTENCE_ARRANGEMENT, // HSK 3/4/5
    HANZI_WRITING,        // HSK 3 — viết chữ Hán
    SHORT_PARAGRAPH,      // HSK 5 Q99 — required_words
    PICTURE_SENTENCE,     // HSK 4
    PICTURE_PARAGRAPH,    // HSK 5 Q100
    SUMMARY_WRITING,      // HSK 6 Q101 — original_article_summary

    // Speaking subtypes → HSK_API /api/v2/speaking/grade
    SPEAKING_LISTEN_AND_ANSWER, // Sơ cấp Part II
    SPEAKING_PICTURE_DESCRIPTION, // Trung cấp Part II
    SPEAKING_READ_ALOUD, // Cao cấp Part II
    SPEAKING_OPEN_ANSWER, // All levels Part III

    // Audio compare → HSK_API /api/v2/audio/compare
    AUDIO_COMPARE
}
