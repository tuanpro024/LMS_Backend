package com.lms.aipractice.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Stores one student answer for one AiPracticeItem within an attempt.
 * Supports text answer (writing) or audio path/base64 (speaking/audio).
 */
@Entity
@Table(name = "ai_practice_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPracticeAnswer extends BaseEntity {

    @Column(name = "attempt_id", nullable = false, length = 26)
    private String attemptId;

    @Column(name = "item_id", nullable = false, length = 26)
    private String itemId;

    /** Text answer — used for writing subtypes */
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    /**
     * Path to uploaded audio file on server — used for speaking/audio subtypes.
     * Adapter will read this file to build multipart or base64.
     */
    @Column(name = "answer_audio_path", length = 500)
    private String answerAudioPath;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
