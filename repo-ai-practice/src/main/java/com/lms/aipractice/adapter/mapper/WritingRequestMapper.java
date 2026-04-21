package com.lms.aipractice.adapter.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
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
        String questionType = resolveQuestionType(item.getQuestionSubtype());
        Integer alignedHskLevel = alignHskLevelWithSubtype(item.getQuestionSubtype(), item.getHskLevel());

        payload.put("question_type", questionType);
        payload.put("hsk_level", alignedHskLevel);
        payload.put("prompt", item.getPromptText());
        payload.put("question", item.getPromptText());
        payload.put("user_input", userInput);
        payload.put("student_answer", userInput);
        payload.put("model", "deepseek-reasoner");
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
                payload.put("required_words", List.of());
            }
        } else {
            payload.put("required_words", List.of());
        }

        return payload;
    }

    private String resolveQuestionType(AiItemSubtype subtype) {
        if (subtype == null) {
            return null;
        }

        return switch (subtype) {
            // All 6 writing types — must match HSK_API QuestionType enum values exactly.
            case SENTENCE_ARRANGEMENT -> "sentence_arrangement";
            case HANZI_WRITING        -> "hanzi_writing";
            case PICTURE_SENTENCE     -> "picture_sentence";
            case SHORT_PARAGRAPH      -> "short_paragraph";
            case PICTURE_PARAGRAPH    -> "picture_paragraph";
            case SUMMARY_WRITING      -> "summary_writing";
            // Keep non-writing subtypes backward compatible.
            default -> subtype.name().toLowerCase();
        };
    }

    private Integer alignHskLevelWithSubtype(AiItemSubtype subtype, Integer inputLevel) {
        if (subtype == null) return inputLevel;

        return switch (subtype) {
            case SHORT_PARAGRAPH -> 5;
            case PICTURE_SENTENCE -> 4;
            case PICTURE_PARAGRAPH -> 5;
            case SUMMARY_WRITING -> 6;
            case HANZI_WRITING -> 3;
            case SENTENCE_ARRANGEMENT -> {
                if (inputLevel != null && (inputLevel == 3 || inputLevel == 4 || inputLevel == 5)) {
                    yield inputLevel;
                }
                yield 3; // Default for sentence arrangement
            }
            default -> inputLevel != null ? inputLevel : 4; // Default generic fallback
        };
    }
}
