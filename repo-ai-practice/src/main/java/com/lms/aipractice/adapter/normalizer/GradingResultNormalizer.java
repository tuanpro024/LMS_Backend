package com.lms.aipractice.adapter.normalizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiGradingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Normalizes raw AI response JSON from HSK_API
 * into a unified AiGradingResult entity.
 *
 * HSK_API Writing response (GradingResponse):
 * score, max_score_per_question, level, deductions, feedback, rubric_breakdown,
 * ...
 *
 * HSK_API Speaking response (SpeakingGradeResponse):
 * final_score, max_score, level, deductions, feedback, transcript,
 * content_analysis, ...
 *
 * HSK_API Audio response (wrapped in result{}):
 * overall_score, feedback, character_comparison, tone_score, initials_score,
 * vowels_score, ...
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GradingResultNormalizer {

    private final ObjectMapper objectMapper;

    public AiGradingResult normalizeWriting(String gradingJobId, String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode data = root.has("result") ? root.path("result") : root;

            Double score = readDouble(data, "score", "final_score", "overall_score");
            Double maxScore = readDouble(data, "max_score_per_question", "max_score");
            if (maxScore == null) {
                maxScore = 100D;
            }

            String level = readText(data, "level");
            String feedback = readText(data, "feedback");
            String deductions = toJsonString(data.path("deductions"));

            // analytics includes rubric_breakdown, required_words_check, character_count
            String analytics = buildWritingAnalytics(data);

            return AiGradingResult.builder()
                    .gradingJobId(gradingJobId)
                    .normalizedScore(score)
                    .normalizedMaxScore(maxScore)
                    .normalizedLevel(level)
                    .feedbackText(feedback)
                    .deductionsJson(deductions)
                    .analyticsJson(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Failed to normalize writing result for job {}: {}", gradingJobId, e.getMessage());
            return errorResult(gradingJobId, e.getMessage());
        }
    }

    public AiGradingResult normalizeSpeaking(String gradingJobId, String rawJson) {
        try {
            // HSK_API speaking result may be nested under "result" if polled from job
            // endpoint
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode data = root.has("result") ? root.path("result") : root;

            Double score = readDouble(data, "final_score", "score", "overall_score");
            Double maxScore = readDouble(data, "max_score", "max_score_per_question");
            if (maxScore == null) {
                maxScore = 20D;
            }

            String level = readText(data, "level");
            String feedback = readText(data, "feedback");
            String transcript = readText(data, "transcript");
            String deductions = toJsonString(data.path("deductions"));

            String analytics = buildSpeakingAnalytics(data);

            return AiGradingResult.builder()
                    .gradingJobId(gradingJobId)
                    .normalizedScore(score)
                    .normalizedMaxScore(maxScore)
                    .normalizedLevel(level)
                    .feedbackText(feedback)
                    .transcriptText(transcript)
                    .deductionsJson(deductions)
                    .analyticsJson(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Failed to normalize speaking result for job {}: {}", gradingJobId, e.getMessage());
            return errorResult(gradingJobId, e.getMessage());
        }
    }

    public AiGradingResult normalizeAudioCompare(String gradingJobId, String rawJson) {
        try {
            // HSK_API audio job response: {job_id, status, result:{...}}
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode data = root.has("result") ? root.path("result") : root;

            // Use overall_score if present, keep full raw payload in analytics for
            // UI/debug.
            double score = data.path("overall_score").asDouble(-1);
            double maxScore = 100;
            String transcript = data.path("student_transcript").asText(
                    data.path("transcript").asText(root.path("transcript").asText("")));
            String feedback = data.path("feedback").asText(root.path("feedback").asText(""));

            String analytics = toJsonString(root);

            return AiGradingResult.builder()
                    .gradingJobId(gradingJobId)
                    .normalizedScore(score >= 0 ? score : null)
                    .normalizedMaxScore(maxScore)
                    .normalizedLevel(score >= 80 ? "Cao" : score >= 60 ? "Trung bình" : "Thấp")
                    .feedbackText(feedback)
                    .transcriptText(transcript)
                    .analyticsJson(analytics)
                    .build();
        } catch (Exception e) {
            log.error("Failed to normalize audio compare result for job {}: {}", gradingJobId, e.getMessage());
            return errorResult(gradingJobId, e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────

    private String buildWritingAnalytics(JsonNode root) throws Exception {
        var analytics = objectMapper.createObjectNode();

        analytics.set("gate_passed", root.path("gate_passed"));
        analytics.set("gate_reason", root.path("gate_reason"));
        analytics.set("cap_applied", root.path("cap_applied"));
        analytics.set("cap_reason", root.path("cap_reason"));
        analytics.set("hsk_level", root.path("hsk_level"));
        analytics.set("result", root.path("result"));
        analytics.set("is_correct", root.path("is_correct"));
        analytics.set("correct_answer", root.path("correct_answer"));
        analytics.set("rubric_breakdown", root.path("rubric_breakdown"));
        analytics.set("required_words_check", root.path("required_words_check"));
        analytics.set("title_check", root.path("title_check"));
        analytics.set("image_relevance", root.path("image_relevance"));
        analytics.set("character_count", root.path("character_count"));
        analytics.set("character_count_valid", root.path("character_count_valid"));
        analytics.set("character_count_note", root.path("character_count_note"));
        analytics.set("missing_source_points", root.path("missing_source_points"));

        analytics.set("grammar_rule_id", root.path("grammar_rule_id"));
        analytics.set("grammar_rule_name", root.path("grammar_rule_name"));
        analytics.set("grammar_explanation", root.path("grammar_explanation"));
        analytics.set("corrected_answer", root.path("corrected_answer"));
        analytics.set("corrected_paragraph", root.path("corrected_paragraph"));
        analytics.set("corrected_summary", root.path("corrected_summary"));
        analytics.set("model_answer", root.path("model_answer"));
        analytics.set("reasoning_content", root.path("reasoning_content"));
        analytics.set("request_id", root.path("request_id"));
        analytics.set("processing_time_ms", root.path("processing_time_ms"));

        return objectMapper.writeValueAsString(analytics);
    }

    private String buildSpeakingAnalytics(JsonNode data) throws Exception {
        var analytics = objectMapper.createObjectNode();
        analytics.set("content_analysis", data.path("content_analysis"));
        analytics.set("raw_transcript", data.path("raw_transcript"));
        analytics.set("total_deducted", data.path("total_deducted"));
        analytics.set("speaking_duration_sec", data.path("speaking_duration_sec"));
        analytics.set("hesitation_count", data.path("hesitation_count"));
        analytics.set("hesitation_total_sec", data.path("hesitation_total_sec"));
        analytics.set("pronunciation_score", data.path("pronunciation_score"));
        analytics.set("relaxed_tone_count", data.path("relaxed_tone_count"));
        analytics.set("pronunciation_meta", data.path("pronunciation_meta"));
        analytics.set("processing_breakdown_ms", data.path("processing_breakdown_ms"));
        analytics.set("asr_model", data.path("asr_model"));
        analytics.set("audio_metadata", data.path("audio_metadata"));
        analytics.set("processing_time_ms", data.path("processing_time_ms"));
        return objectMapper.writeValueAsString(analytics);
    }

    private String toJsonString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(
                    node == null || node.isMissingNode() || node.isNull() ? objectMapper.createArrayNode() : node);
        } catch (Exception e) {
            return "[]";
        }
    }

    private Double readDouble(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);
            if (!value.isMissingNode() && !value.isNull() && value.isNumber()) {
                return value.asDouble();
            }
        }
        return null;
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text != null && !text.isBlank() ? text : null;
    }

    private AiGradingResult errorResult(String gradingJobId, String error) {
        return AiGradingResult.builder()
                .gradingJobId(gradingJobId)
                .normalizedScore(0.0)
                .normalizedMaxScore(100.0)
                .normalizedLevel("Thấp")
                .feedbackText("Lỗi xử lý kết quả: " + error)
                .build();
    }
}
