package com.lms.aipractice.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.time.Duration;

/**
 * Client for ASR_HSK-main (FastAPI CAPT system).
 * Endpoint: POST /evaluate  — multipart/form-data
 *   - audio: WAV file
 *   - expected_text: Hanzi string (reference_text from AiPracticeItem)
 *
 * Returns synchronously — no job_id polling needed.
 */
@Component
@Slf4j
public class AsrHskClient {

    private final WebClient webClient;
    private final long timeoutMs;

    public AsrHskClient(
            @Value("${app.ai.asr-hsk.base-url}") String baseUrl,
            @Value("${app.ai.asr-hsk.timeout-ms}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Evaluate pronunciation by comparing student audio against reference text.
     * @param audioFilePath absolute path to the student WAV file
     * @param expectedText  Hanzi reference text (from AiPracticeItem.referenceText)
     * @return raw JSON response string from ASR_HSK
     */
    public String evaluate(String audioFilePath, String expectedText) {
        log.debug("Calling ASR_HSK /evaluate for text: {}", expectedText);

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Audio file not found: " + audioFilePath);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new FileSystemResource(audioFile));
        body.add("expected_text", expectedText);

        return webClient.post()
                .uri("/evaluate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();
    }
}
