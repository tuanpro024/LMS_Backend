package com.lms.aipractice.adapter.normalizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiGradingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Normalizes raw AI response JSON (from either HSK_API or ASR_HSK)
 * into a unified AiGradingResult entity.
 *
 * HSK_API Writing response (GradingResponse):
 *   score, max_score_per_question, level, deductions, feedback, rubric_breakdown, ...
 *
 * HSK_API Speaking response (SpeakingGradeResponse):
 *   final_score, max_score, level, deductions, feedback, transcript, content_analysis, ...
 *
 * ASR_HSK response (wrapped in result{}):
 *   overall_score, feedback, character_comparison, tone_score, initials_score, vowels_score, ...
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GradingResultNormalizer {

    private final ObjectMapper objectMapper;

    public AiGradingResult normalizeWriting(String gradingJobId, String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            double score = root.path("score").asDouble(0);
            double maxScore = root.path("max_score_per_question").asDouble(100);
            String level = root.path("level").asText("");
            String feedback = root.path("feedback").asText("");
            String deductions = toJsonString(root.path("deductions"));

            // analytics includes rubric_breakdown, required_words_check, character_count
            String analytics = buildWritingAnalytics(root);

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
            // HSK_API speaking result may be nested under "result" if polled from job endpoint
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode data = root.has("result") ? root.path("result") : root;

            double score = data.path("final_score").asDouble(0);
            double maxScore = data.path("max_score").asDouble(20);
            String level = data.path("level").asText("");
            String feedback = data.path("feedback").asText("");
            String transcript = data.path("transcript").asText(null);
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
            // ASR_HSK response: {status, processing_time_sec, details:[{hanzi, tone_feedback, ...}]}
            JsonNode root = objectMapper.readTree(rawJson);

            // ASR_HSK returns per-character details — aggregate to overall score
            // Use overall_score if present (HSK_API audio_practice path), else derive from details
            double score = root.path("result").path("overall_score").asDouble(-1);
            double maxScore = 100;
            String transcript = root.path("result").path("student_transcript").asText(
                    root.path("transcript").asText(""));
            String feedback = root.path("result").path("feedback").asText("");

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
        analytics.set("rubric_breakdown", root.path("rubric_breakdown"));
        analytics.set("required_words_check", root.path("required_words_check"));
        analytics.set("title_check", root.path("title_check"));
        analytics.put("character_count", root.path("character_count").asInt(0));
        analytics.put("character_count_valid", root.path("character_count_valid").asBoolean(true));
        analytics.put("character_count_note", root.path("character_count_note").asText(""));
        analytics.set("missing_source_points", root.path("missing_source_points"));
        analytics.put("is_correct", root.path("is_correct").asBoolean(false));
        return objectMapper.writeValueAsString(analytics);
    }

    private String buildSpeakingAnalytics(JsonNode data) throws Exception {
        var analytics = objectMapper.createObjectNode();
        analytics.set("content_analysis", data.path("content_analysis"));
        analytics.put("speaking_duration_sec", data.path("speaking_duration_sec").asDouble(0));
        analytics.put("hesitation_count", data.path("hesitation_count").asInt(0));
        analytics.put("hesitation_total_sec", data.path("hesitation_total_sec").asDouble(0));
        analytics.put("pronunciation_score", data.path("pronunciation_score").asDouble(0));
        analytics.set("processing_breakdown_ms", data.path("processing_breakdown_ms"));
        analytics.put("asr_model", data.path("asr_model").asText(""));
        return objectMapper.writeValueAsString(analytics);
    }

    private String toJsonString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node.isMissingNode() ? objectMapper.createArrayNode() : node);
        } catch (Exception e) {
            return "[]";
        }
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
