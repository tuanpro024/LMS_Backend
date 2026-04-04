package com.lms.aipractice.adapter.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps AiPracticeItem + AiPracticeAnswer → request payload for HSK_API
 * /api/v2/grade.
 *
 * Field mapping per AI_JSON writing input schema:
 * question_type ← questionSubtype (lowercase)
 * hsk_level ← hskLevel
 * question ← promptText
 * student_answer ← answerText
 * required_words ← requiredWordsJson (parsed to List<String>)
 * image_description ← imageDescription
 * reference_answer ← referenceAnswer
 * original_article_summary ← originalArticleSummary
 * sentence_context, student_audio_base64, student_audio_path → null
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WritingRequestMapper {

    private final ObjectMapper objectMapper;

    public Map<String, Object> buildPayload(AiPracticeItem item, AiPracticeAnswer answer) {
        Map<String, Object> payload = new HashMap<>();
        String userInput = answer.getAnswerText() != null ? answer.getAnswerText() : "";

        payload.put("question_type", item.getQuestionSubtype().name().toLowerCase());
        payload.put("hsk_level", item.getHskLevel());
        payload.put("prompt", item.getPromptText());
        payload.put("question", item.getPromptText());
        payload.put("user_input", userInput);
        payload.put("student_answer", userInput);
        payload.put("model", "deepseek-chat");
        payload.put("reference_answer", item.getReferenceAnswer());
        payload.put("image_description", item.getImageDescription());
        payload.put("original_article_summary", item.getOriginalArticleSummary());
        payload.put("sentence_context", null);
        payload.put("student_audio_base64", null);
        payload.put("student_audio_path", null);
        payload.put("target_model_override", null);

        // Parse required_words JSON array
        if (item.getRequiredWordsJson() != null && !item.getRequiredWordsJson().isBlank()) {
            try {
                List<String> words = objectMapper.readValue(
                        item.getRequiredWordsJson(), new TypeReference<>() {
                        });
                payload.put("required_words", words);
            } catch (Exception e) {
                log.warn("Failed to parse requiredWordsJson for item {}: {}", item.getId(), e.getMessage());
                payload.put("required_words", null);
            }
        } else {
            payload.put("required_words", null);
        }

        return payload;
    }
}
