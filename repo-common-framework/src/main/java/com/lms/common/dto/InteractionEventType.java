package com.lms.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum InteractionEventType {
    PAGE_VIEW("page_view"),
    LESSON_START("lesson_start"),
    LESSON_COMPLETE("lesson_complete"),
    FLASHCARD_FLIP("flashcard_flip"),
    FLASHCARD_STUDY_COMPLETE("flashcard_study_complete"),
    VIDEO_PLAY("video_play"),
    VIDEO_PAUSE("video_pause"),
    VIDEO_COMPLETE("video_complete"),
    QUIZ_START("quiz_start"),
    QUIZ_SUBMIT("quiz_submit"),
    AI_PRACTICE_START("ai_practice_start"),
    SEARCH("search"),
    COURSE_ENROLL("course_enroll"),
    PAYMENT_SUCCESS("payment_success"),
    START("start"),
    AUDIO_PLAY("audio_play"),
    COPY_TEXT("copy_text");

    private final String value;

    InteractionEventType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static InteractionEventType fromString(String value) {
        if (value == null) return null;
        String normalized = value.toLowerCase().trim();
        for (InteractionEventType type : values()) {
            if (type.value.equalsIgnoreCase(normalized)) return type;
        }
        throw new IllegalArgumentException("Unknown interaction event type: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
