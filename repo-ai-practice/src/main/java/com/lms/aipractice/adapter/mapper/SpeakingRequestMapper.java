package com.lms.aipractice.adapter.mapper;

import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps AiPracticeItem + AiPracticeAnswer → request payload for HSK_API
 * /api/v2/speaking/grade.
 *
 * Field mapping per AI_JSON speaking input schema (models_speaking.py
 * SpeakingGradeRequest):
 * part_type ← questionSubtype → e.g. "listen_and_answer"
 * level ← partLevel → "elementary" / "intermediate" / "advanced"
 * student_audio_base64 ← read audio file and base64-encode
 * student_audio_path ← null (we use base64)
 * question_text ← promptText
 * reference_answer ← referenceAnswer
 * image_description ← imageDescription
 * passage_summary ← null (not used in current subtypes)
 * target_model_override ← null
 */
@Component
@Slf4j
public class SpeakingRequestMapper {

    public Map<String, Object> buildPayload(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = new HashMap<>();

        // Convert enum "SPEAKING_LISTEN_AND_ANSWER" → "listen_and_answer"
        String partType = item.getQuestionSubtype().name()
                .replace("SPEAKING_", "")
            .toLowerCase(Locale.ROOT);
        String level = alignLevelWithAiLmsRubric(partType, normalizeLevel(item.getPartLevel()));
        payload.put("part_type", partType);
        payload.put("level", level);
        payload.put("question_text", item.getPromptText());
        payload.put("reference_answer", item.getReferenceAnswer());
        payload.put("image_description", item.getImageDescription());
        payload.put("passage_summary", null);
        payload.put("target_model_override", null);

        // Encode audio to base64
        if (answer.getAnswerAudioPath() != null) {
            try {
                byte[] audioBytes = Files.readAllBytes(Path.of(answer.getAnswerAudioPath()));
                payload.put("student_audio_base64", Base64.getEncoder().encodeToString(audioBytes));
            } catch (IOException e) {
                log.error("Cannot read audio file for speaking job: {}", answer.getAnswerAudioPath(), e);
                throw new RuntimeException("Cannot read audio file: " + answer.getAnswerAudioPath(), e);
            }
        } else {
            payload.put("student_audio_base64", null);
        }
        payload.put("student_audio_path", null);

        return payload;
    }

    private String normalizeLevel(String rawLevel) {
        if (rawLevel == null || rawLevel.isBlank()) {
            return "elementary";
        }

        String normalized = rawLevel.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "elementary", "beginner", "basic", "so_cap", "sơ cấp" -> "elementary";
            case "intermediate", "trung_cap", "trung cấp" -> "intermediate";
            case "advanced", "cao_cap", "cao cấp" -> "advanced";
            default -> normalized;
        };
    }

    private String alignLevelWithAiLmsRubric(String partType, String normalizedLevel) {
        // Keep level-part matrix consistent with HSK_API RUBRIC_CONFIGS.
        return switch (partType) {
            case "listen_and_answer" -> "elementary";
            case "picture_description" -> "intermediate";
            case "read_aloud" -> "advanced";
            case "open_answer" -> switch (normalizedLevel) {
                case "elementary", "intermediate", "advanced" -> normalizedLevel;
                default -> "elementary";
            };
            default -> normalizedLevel;
        };
    }
}
