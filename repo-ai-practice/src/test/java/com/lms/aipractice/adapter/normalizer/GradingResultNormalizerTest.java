package com.lms.aipractice.adapter.normalizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiGradingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradingResultNormalizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private GradingResultNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new GradingResultNormalizer(objectMapper);
    }

    @Test
    void normalizeWriting_readsWrappedResultAndPreservesNullableAnalytics() throws Exception {
        String rawJson = """
                {
                  "job_id": "job_1",
                  "status": "completed",
                  "result": {
                    "score": 22,
                    "max_score_per_question": 30,
                    "level": "Trung bình",
                    "deductions": [{"category": "Lỗi ngữ pháp", "points_deducted": 2}],
                    "feedback": "Bài viết ổn",
                    "character_count": null,
                    "character_count_valid": null,
                    "required_words_check": {"usage_ratio": "3/4"},
                    "corrected_paragraph": "Đoạn đã sửa"
                  }
                }
                """;

        AiGradingResult result = normalizer.normalizeWriting("grading_job_1", rawJson);
        JsonNode analytics = objectMapper.readTree(result.getAnalyticsJson());

        assertEquals("grading_job_1", result.getGradingJobId());
        assertEquals(22D, result.getNormalizedScore());
        assertEquals(30D, result.getNormalizedMaxScore());
        assertEquals("Trung bình", result.getNormalizedLevel());
        assertEquals("Bài viết ổn", result.getFeedbackText());

        assertNotNull(result.getDeductionsJson());
        assertTrue(analytics.path("character_count").isNull());
        assertTrue(analytics.path("character_count_valid").isNull());
        assertEquals("3/4", analytics.path("required_words_check").path("usage_ratio").asText());
        assertEquals("Đoạn đã sửa", analytics.path("corrected_paragraph").asText());
    }

    @Test
    void normalizeSpeaking_readsScoreAndKeepsSpeakingAnalytics() throws Exception {
        String rawJson = """
                {
                  "result": {
                    "final_score": 16,
                    "max_score": 20,
                    "level": "Cao",
                    "feedback": "Nói tốt",
                    "transcript": "我的生日是七月十五号",
                    "deductions": [{"category": "content", "points_deducted": 2}],
                    "content_analysis": {"content_quality": "partial"},
                    "hesitation_count": 2,
                    "speaking_duration_sec": 3.2,
                    "audio_metadata": {"duration_sec": 3.2}
                  }
                }
                """;

        AiGradingResult result = normalizer.normalizeSpeaking("grading_job_2", rawJson);
        JsonNode analytics = objectMapper.readTree(result.getAnalyticsJson());

        assertEquals(16D, result.getNormalizedScore());
        assertEquals(20D, result.getNormalizedMaxScore());
        assertEquals("Cao", result.getNormalizedLevel());
        assertEquals("Nói tốt", result.getFeedbackText());
        assertEquals("我的生日是七月十五号", result.getTranscriptText());
        assertEquals("partial", analytics.path("content_analysis").path("content_quality").asText());
        assertEquals(2, analytics.path("hesitation_count").asInt());
        assertEquals(3.2D, analytics.path("audio_metadata").path("duration_sec").asDouble());
    }

    @Test
    void normalizeAudioCompare_handlesWrappedAndUnwrappedPayloads() throws Exception {
        String wrapped = """
                {
                  "status": "completed",
                  "result": {
                    "overall_score": 85,
                    "student_transcript": "你好，我叫王小明",
                    "feedback": "Phát âm tốt"
                  }
                }
                """;

        AiGradingResult wrappedResult = normalizer.normalizeAudioCompare("grading_job_3", wrapped);
        JsonNode wrappedAnalytics = objectMapper.readTree(wrappedResult.getAnalyticsJson());

        assertEquals(85D, wrappedResult.getNormalizedScore());
        assertEquals(100D, wrappedResult.getNormalizedMaxScore());
        assertEquals("Cao", wrappedResult.getNormalizedLevel());
        assertEquals("Phát âm tốt", wrappedResult.getFeedbackText());
        assertEquals("你好，我叫王小明", wrappedResult.getTranscriptText());
        assertEquals("completed", wrappedAnalytics.path("status").asText());

        String unwrapped = """
                {
                  "overall_score": 62,
                  "transcript": "你好",
                  "feedback": "Tạm ổn"
                }
                """;

        AiGradingResult unwrappedResult = normalizer.normalizeAudioCompare("grading_job_4", unwrapped);

        assertEquals(62D, unwrappedResult.getNormalizedScore());
        assertEquals("Trung bình", unwrappedResult.getNormalizedLevel());
        assertEquals("你好", unwrappedResult.getTranscriptText());
        assertEquals("Tạm ổn", unwrappedResult.getFeedbackText());
    }
}
