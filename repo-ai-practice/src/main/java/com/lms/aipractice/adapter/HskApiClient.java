package com.lms.aipractice.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client for HSK_API-main (FastAPI).
 * Handles:
 * - Writing: POST /api/v2/grade (async), fallback /api/v1/grade (sync)
 * - Speaking: POST /api/v2/speaking/grade/sync (sync), optional async
 * /api/v2/speaking/grade
 * - Audio compare: POST /api/v2/audio/compare (async)
 * - Polling: GET /api/v2/job/{job_id}, /api/v2/speaking/job/{job_id},
 * /api/v2/audio/job/{job_id}
 */
@Component
@Slf4j
public class HskApiClient {

    private final WebClient webClient;
    private final long timeoutMs;
    private final ObjectMapper objectMapper;

    public HskApiClient(
            @Value("${app.ai.hsk-api.base-url}") String baseUrl,
            @Value("${app.ai.hsk-api.timeout-ms}") long timeoutMs,
            ObjectMapper objectMapper) {
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Submit a writing grading job.
     * 
     * @param payload Map matching AI_JSON writing input schema
     * @return provider job_id
     */
    public String submitWritingJob(Map<String, Object> payload) {
        log.debug("Submitting writing job to HSK_API: {}", payload.get("question_type"));
        return postGradeJob("/api/v2/grade", payload);
    }

    /**
     * Submit a speaking grading job.
     * 
     * @param payload Map matching AI_JSON speaking input schema
     * @return provider job_id
     */
    public String submitSpeakingJob(Map<String, Object> payload) {
        log.debug("Submitting speaking job to HSK_API: {}", payload.get("part_type"));
        return postGradeJob("/api/v2/speaking/grade", payload);
    }

    /**
     * Submit an audio-compare grading job.
     *
     * @param payload Map matching audio compare request schema
     * @return provider job_id
     */
    public String submitAudioCompareJob(Map<String, Object> payload) {
        log.debug("Submitting audio compare job to HSK_API");
        return postGradeJob("/api/v2/audio/compare", payload);
    }

    /**
     * Fallback for deployments where speaking async route is unavailable.
     * Returns full grading JSON (sync response, no job_id).
     */
    public String submitSpeakingSyncV2(Map<String, Object> payload) {
        log.warn("Falling back to speaking sync endpoint: /api/v2/speaking/grade/sync");
        return postRawJson("/api/v2/speaking/grade/sync", payload);
    }

    /**
     * Fallback for deployments that only expose legacy v1 writing API.
     * Returns full grading JSON (sync response, no job_id).
     */
    public String submitWritingSyncV1(Map<String, Object> payload) {
        Map<String, Object> v1Payload = new HashMap<>();
        v1Payload.put("question_type", payload.get("question_type"));
        v1Payload.put("hsk_level", payload.get("hsk_level"));
        v1Payload.put("question", payload.get("question") != null ? payload.get("question") : payload.get("prompt"));
        v1Payload.put("student_answer",
                payload.get("student_answer") != null ? payload.get("student_answer") : payload.get("user_input"));
        v1Payload.put("reference_answer", payload.get("reference_answer"));
        v1Payload.put("required_words", payload.get("required_words"));
        v1Payload.put("image_description", payload.get("image_description"));
        v1Payload.put("sentence_context", payload.get("sentence_context"));
        v1Payload.put("original_article_summary", payload.get("original_article_summary"));
        v1Payload.put("target_model_override",
                payload.get("target_model_override") != null ? payload.get("target_model_override")
                        : payload.get("model"));

        log.warn("Falling back to legacy HSK_API v1 endpoint: /api/v1/grade");
        return postRawJson("/api/v1/grade", v1Payload);
    }

    private String postGradeJob(String path, Map<String, Object> payload) {
        String response = postRawJson(path, payload);

        try {
            JsonNode node = objectMapper.readTree(response);
            JsonNode jobIdNode = node.get("job_id");
            if (jobIdNode == null || jobIdNode.asText().isBlank()) {
                throw new RuntimeException("Missing job_id in HSK_API response: " + response);
            }
            return jobIdNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse job_id from HSK_API response: " + response, e);
        }
    }

    private String postRawJson(String path, Map<String, Object> payload) {
        return webClient.post()
                .uri(path)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new RuntimeException(
                                "HSK_API call failed (" + clientResponse.statusCode().value() + ") "
                                        + path + " -> " + body)))
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();
    }

    /**
     * Poll job result from HSK_API.
     * 
     * @return raw JSON string of full job response, or empty if still pending
     */
    public Optional<String> pollJobResult(String jobId) {
        log.debug("Polling HSK_API job: {}", jobId);
        try {
            String response;
            try {
                response = getRawJson("/api/v2/job/{jobId}", jobId);
            } catch (RuntimeException exV2) {
                if (!isNotFound(exV2.getMessage(), "/api/v2/job")) {
                    throw exV2;
                }

                try {
                    response = getRawJson("/api/v2/speaking/job/{jobId}", jobId);
                } catch (RuntimeException exSpeaking) {
                    if (!isNotFound(exSpeaking.getMessage(), "/api/v2/speaking/job")) {
                        throw exSpeaking;
                    }
                    response = getRawJson("/api/v2/audio/job/{jobId}", jobId);
                }
            }

            JsonNode node = objectMapper.readTree(response);
            String status = node.path("status").asText();

            if ("completed".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
                return Optional.of(response);
            }
            return Optional.empty(); // still pending/processing
        } catch (Exception e) {
            log.error("Error polling HSK_API job {}: {}", jobId, e.getMessage());
            return Optional.empty();
        }
    }

    private String getRawJson(String path, String jobId) {
        return webClient.get()
                .uri(path, jobId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new RuntimeException(
                                "HSK_API call failed (" + clientResponse.statusCode().value() + ") "
                                        + path + " -> " + body)))
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(10_000))
                .block();
    }

    private boolean isNotFound(String message, String endpointPath) {
        return message != null && message.contains("(404)") && message.contains(endpointPath);
    }
}
