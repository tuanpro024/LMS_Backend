package com.lms.onllearning.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.TypeName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * WebClient wrapper để gọi các repo ôn luyện (flashcard, writing, kanji, pronunciation, quiz).
 * <p>
 * - GET /study-sets  → lấy danh sách study set có sẵn (dùng InternalApiClient ở service)
 * - POST /packages/import-excel (multipart) → import Excel tạo nội dung mới
 * <p>
 * Không dùng Feign vì cần forward MultipartFile; WebClient (đã có trong project) đơn giản hơn.
 */
@Component
@Slf4j
public class PracticeModuleWebClient {

    /** WebClient KHÔNG có baseUrl cố định — URL được build từ Eureka service-name pattern. */
    private final WebClient.Builder webClientBuilder;

    public PracticeModuleWebClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private String getAuthHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    /**
     * Gọi POST /packages/import-excel trên repo ngoài rồi trả về HierarchicalImportResult.
     *
     * @param serviceBaseUrl URL của service đích, ví dụ "http://repo-flashcard"
     * @param file           File Excel do client upload
     * @param typeName       Loại nội dung (LEARNING, FREE, …)
     */
    public HierarchicalImportResult importExcel(String serviceBaseUrl,
                                                 MultipartFile file,
                                                 TypeName typeName) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource());
            builder.part("typeName", typeName.name());

            String authHeader = getAuthHeader();

            // Deserialize ApiResponse<HierarchicalImportResult>
            String raw = webClientBuilder.build()
                    .post()
                    .uri(serviceBaseUrl + "/packages/import-excel")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Import failed (500): " + body)))
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();

            ObjectMapper om = new ObjectMapper();
            ApiResponse<HierarchicalImportResult> resp = om.readValue(
                    raw, new TypeReference<>() {});

            if (resp == null || resp.data() == null) {
                return HierarchicalImportResult.builder().build();
            }
            return resp.data();

        } catch (Exception e) {
            log.error("PracticeModuleWebClient.importExcel({}) failed: {}", serviceBaseUrl, e.getMessage());
            throw new RuntimeException("Không thể kết nối đến dịch vụ ôn luyện để import: " + e.getMessage(), e);
        }
    }

    /**
     * GET /study-sets/{id} từ repo ngoài.
     *
     * @param serviceBaseUrl URL base của service (e.g. "http://repo-flashcard")
     * @param id             Study set ID cần kiểm tra
     * @return StudySetResponse nếu tồn tại, null nếu không tìm thấy (404)
     */
    public StudySetResponse getStudySetById(String serviceBaseUrl, String id) {
        try {
            if (id == null || id.isBlank()) {
                return null;
            }

            String authHeader = getAuthHeader();
            String raw = webClientBuilder.build()
                    .get()
                    .uri(serviceBaseUrl + "/study-sets/" + id)
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();

            if (raw == null) {
                return null;
            }

            ObjectMapper om = new ObjectMapper();
            om.findAndRegisterModules();
            ApiResponse<StudySetResponse> resp = om.readValue(raw, new TypeReference<>() {});
            return resp != null ? resp.data() : null;
        } catch (WebClientResponseException.NotFound ex) {
            return null;
        } catch (Exception e) {
            log.error("PracticeModuleWebClient.getStudySetById({}, {}) failed: {}", serviceBaseUrl, id, e.getMessage());
            throw new RuntimeException("Không thể kiểm tra study set từ dịch vụ ôn luyện: " + e.getMessage(), e);
        }
    }

    /**
     * GET /study-sets?q= từ repo ngoài.
     *
     * @param serviceBaseUrl URL base của service (e.g. "http://repo-flashcard")
     * @param query          Từ khoá tìm kiếm, null nếu lấy tất cả
     */
    public List<StudySetResponse> getStudySets(String serviceBaseUrl, String query) {
        try {
            String uri = (query != null && !query.isBlank())
                    ? serviceBaseUrl + "/study-sets?q=" + query
                    : serviceBaseUrl + "/study-sets";

            String authHeader = getAuthHeader();

            String raw = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();

            if (raw == null) return new ArrayList<>();

            ObjectMapper om = new ObjectMapper();
            // register JavaTimeModule for Instant deserialization
            om.findAndRegisterModules();
            ApiResponse<List<StudySetResponse>> resp = om.readValue(
                    raw, new TypeReference<>() {});

            return (resp != null && resp.data() != null) ? resp.data() : new ArrayList<>();

        } catch (Exception e) {
            log.warn("PracticeModuleWebClient.getStudySets({}, q={}) failed: {}", serviceBaseUrl, query, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Double getFlashcardProgressPercentage(String serviceBaseUrl, String studySetId) {
        try {
            String raw = getRaw(serviceBaseUrl + "/study-sets/" + studySetId + "/progress");
            if (raw == null || raw.isBlank()) {
                return null;
            }
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(raw);
            return normalizeToPercentage(extractDouble(root, "progressPercentage"));
        } catch (Exception ex) {
            log.warn("Failed to fetch flashcard progress for studySetId={}: {}", studySetId, ex.getMessage());
            return null;
        }
    }

    public Double getKanjiProgressPercentage(String serviceBaseUrl, String studySetId) {
        try {
            String raw = getRaw(serviceBaseUrl + "/study-sets/" + studySetId + "/progress");
            if (raw == null || raw.isBlank()) {
                return null;
            }
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(raw);
            JsonNode data = root.path("data");
            return normalizeToPercentage(extractDouble(data, "progressPercentage"));
        } catch (Exception ex) {
            log.warn("Failed to fetch kanji progress for studySetId={}: {}", studySetId, ex.getMessage());
            return null;
        }
    }

    public Double getPronunciationProgressPercentage(String serviceBaseUrl, String studySetId) {
        try {
            String raw = getRaw(serviceBaseUrl + "/pronunciation-progress/study-sets/" + studySetId);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(raw);
            JsonNode data = root.path("data");
            return normalizeToPercentage(extractDouble(data, "progressPercentage"));
        } catch (Exception ex) {
            log.warn("Failed to fetch pronunciation progress for studySetId={}: {}", studySetId, ex.getMessage());
            return null;
        }
    }

    public Double getWritingProgressPercentage(String serviceBaseUrl, String studySetId) {
        try {
            String learnedRaw = getRaw(serviceBaseUrl + "/study-sets/" + studySetId + "/count/learned");
            String unlearnedRaw = getRaw(serviceBaseUrl + "/study-sets/" + studySetId + "/count/unlearned");
            if (learnedRaw == null || unlearnedRaw == null) {
                return null;
            }

            ObjectMapper om = new ObjectMapper();
            JsonNode learnedRoot = om.readTree(learnedRaw);
            JsonNode unlearnedRoot = om.readTree(unlearnedRaw);

            long learned = extractLong(learnedRoot.path("data"));
            long unlearned = extractLong(unlearnedRoot.path("data"));
            long total = learned + unlearned;
            if (total <= 0) {
                return 0.0;
            }

            return (learned * 100.0) / total;
        } catch (Exception ex) {
            log.warn("Failed to fetch writing progress for studySetId={}: {}", studySetId, ex.getMessage());
            return null;
        }
    }

    public Double getQuizStudySetProgressPercentage(String serviceBaseUrl, String studySetId) {
        try {
            String quizzesRaw = getRaw(serviceBaseUrl + "/quizzes/study-set/" + studySetId);
            if (quizzesRaw == null || quizzesRaw.isBlank()) {
                return null;
            }

            ObjectMapper om = new ObjectMapper();
            JsonNode quizzesRoot = om.readTree(quizzesRaw);
            JsonNode quizzes = quizzesRoot.path("data");
            if (!quizzes.isArray() || quizzes.isEmpty()) {
                return 0.0;
            }

            int total = 0;
            int completed = 0;

            for (JsonNode quiz : quizzes) {
                String quizId = extractString(quiz, "id");
                if (quizId == null || quizId.isBlank()) {
                    continue;
                }
                total++;

                String progressRaw = getRaw(serviceBaseUrl + "/attempts/quizzes/" + quizId + "/progress");
                if (progressRaw == null || progressRaw.isBlank()) {
                    continue;
                }
                JsonNode progressRoot = om.readTree(progressRaw);
                JsonNode progressData = progressRoot.path("data");
                if (progressData.path("completed").asBoolean(false)) {
                    completed++;
                }
            }

            if (total <= 0) {
                return 0.0;
            }
            return (completed * 100.0) / total;
        } catch (Exception ex) {
            log.warn("Failed to fetch quiz progress for studySetId={}: {}", studySetId, ex.getMessage());
            return null;
        }
    }

    private String getRaw(String url) {
        String authHeader = getAuthHeader();
        return webClientBuilder.build()
                .get()
                .uri(url)
                .headers(h -> {
                    if (authHeader != null) {
                        h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    }
                })
                .retrieve()
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .block();
    }

    private Double extractDouble(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode value = fieldName == null ? node : node.path(fieldName);
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual()) {
            try {
                return Double.parseDouble(value.asText().trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private long extractLong(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0L;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (Exception ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String extractString(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null ? null : text.trim();
    }

    private Double normalizeToPercentage(Double value) {
        if (value == null) {
            return null;
        }
        double normalized = value;
        if (Math.abs(normalized) <= 1.0d) {
            normalized *= 100.0d;
        }
        if (normalized < 0) {
            normalized = 0;
        }
        if (normalized > 100) {
            normalized = 100;
        }
        return Double.valueOf(String.format(Locale.ROOT, "%.2f", normalized));
    }
}
