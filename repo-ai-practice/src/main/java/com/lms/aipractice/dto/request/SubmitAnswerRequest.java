package com.lms.aipractice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitAnswerRequest {

    @NotBlank
    private String itemId;

    /** Text answer — required for writing subtypes */
    private String answerText;

    /**
     * Audio file path on server (set by controller after multipart upload).
     * For speaking/audio subtypes only.
     */
    private String answerAudioPath;
}
