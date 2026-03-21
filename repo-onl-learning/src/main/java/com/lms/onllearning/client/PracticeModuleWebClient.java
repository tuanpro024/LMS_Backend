package com.lms.onllearning.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
}
