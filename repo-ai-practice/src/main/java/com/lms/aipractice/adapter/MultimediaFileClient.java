package com.lms.aipractice.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;

/**
 * Minimal client for repo-multimedia file upload endpoint.
 */
@Component
@Slf4j
public class MultimediaFileClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final long timeoutMs;
    private final String publicBaseUrl;

    public MultimediaFileClient(
            @Value("${app.multimedia.base-url}") String baseUrl,
            @Value("${app.multimedia.public-base-url}") String publicBaseUrl,
            @Value("${app.multimedia.timeout-ms:30000}") long timeoutMs,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.timeoutMs = timeoutMs;
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.webClient = WebClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .build();
    }

    /**
     * Uploads a file to repo-multimedia and returns generated file id.
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "File is required");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        String rawResponse = webClient.post()
                .uri("/api/media/file/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(err -> new ApiException(
                                ErrorCode.E305,
                                "Multimedia upload failed (" + clientResponse.statusCode().value() + "): " + err)))
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode fileIdNode = root.path("data").path("fileId");
            if (fileIdNode.isMissingNode() || fileIdNode.asText().isBlank()) {
                throw new ApiException(ErrorCode.E305, "Multimedia upload response missing fileId");
            }
            return fileIdNode.asText();
        } catch (IOException ex) {
            log.error("Cannot parse multimedia upload response: {}", rawResponse, ex);
            throw new ApiException(ErrorCode.E305, "Invalid multimedia upload response", ex);
        }
    }

    public String buildFileAccessUrl(String fileId) {
        return publicBaseUrl + "/api/media/file/" + fileId;
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            final byte[] bytes = file.getBytes();
            final String filename = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                    ? "audio-file"
                    : file.getOriginalFilename();

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot read uploaded file", ex);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
