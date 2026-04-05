package com.lms.onllearning.client;

import com.lms.onllearning.dto.response.CmsApiResponse;
import com.lms.onllearning.dto.response.CmsCourseTimetableResponse;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.CmsOnlineCourseResponse;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client gọi CMS API: https://cms.dangch.tech
 * CMS trả format: {"statusCode":200,"data":...,"message":...,"success":true}
 * Các API catalogue có fallback Redis; API sync timetable full snapshot thì
 * không dùng cache.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsClient {

    private final WebClient cmsWebClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_SYLLABUS_LIST = "syllabus:list:all";
    private static final String CACHE_KEY_SYLLABUS_DETAIL = "syllabus:detail:";
    private static final String CACHE_KEY_COURSE_LIST = "course:list:type:";
    private static final long SYLLABUS_TTL_SEC = 600L;

    // -----------------------------------------------------------------------
    // Syllabus List — GET /api/erp/syllabus
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<SyllabusResponse>> getSyllabuses() {
        String cacheKey = CACHE_KEY_SYLLABUS_LIST;
        try {
            CmsApiResponse<List<SyllabusResponse>> resp = cmsWebClient.get()
                    .uri("/api/erp/syllabus")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<SyllabusResponse>>>() {
                    })
                    .block();

            List<SyllabusResponse> data = resp != null ? resp.data() : List.of();
            try {
                redisTemplate.opsForValue().set(cacheKey, data, SYLLABUS_TTL_SEC, TimeUnit.SECONDS);
            } catch (Exception cacheEx) {
                log.warn("CMS getSyllabuses: cache write failed, continue with CMS data: {}", cacheEx.getMessage());
            }
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getSyllabuses failed, falling back to cache: {}", e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((List<SyllabusResponse>) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }

    // -----------------------------------------------------------------------
    // Syllabus Detail — GET /api/erp/syllabus/{id}
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId) {
        String cacheKey = CACHE_KEY_SYLLABUS_DETAIL + syllabusId;
        try {
            CmsApiResponse<SyllabusDetailResponse> resp = cmsWebClient.get()
                    .uri("/api/erp/syllabus/{id}", syllabusId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<SyllabusDetailResponse>>() {
                    })
                    .block();

            SyllabusDetailResponse data = resp != null ? resp.data() : null;
            if (data != null) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, data, SYLLABUS_TTL_SEC, TimeUnit.SECONDS);
                } catch (Exception cacheEx) {
                    log.warn("CMS getSyllabusDetail({}): cache write failed, continue with CMS data: {}",
                            syllabusId, cacheEx.getMessage());
                }
            }
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getSyllabusDetail({}) failed, falling back to cache: {}", syllabusId, e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((SyllabusDetailResponse) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }

    // -----------------------------------------------------------------------
    // Online Courses - GET /api/erp/courses/list?type={type}
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<CmsOnlineCourseResponse>> getOnlineCoursesByType(String type) {
        String normalizedType = normalizeCourseType(type);
        String cacheKey = CACHE_KEY_COURSE_LIST + normalizedType;
        try {
            CmsApiResponse<List<CmsOnlineCourseResponse>> resp = cmsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/erp/courses/list")
                            .queryParam("type", normalizedType)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<CmsOnlineCourseResponse>>>() {
                    })
                    .block();

            List<CmsOnlineCourseResponse> data = resp != null && resp.data() != null ? resp.data() : List.of();
            try {
                redisTemplate.opsForValue().set(cacheKey, data, SYLLABUS_TTL_SEC, TimeUnit.SECONDS);
            } catch (Exception cacheEx) {
                log.warn("CMS getOnlineCoursesByType(type={}): cache write failed, continue with CMS data: {}",
                        normalizedType, cacheEx.getMessage());
            }
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getOnlineCoursesByType(type={}) failed, falling back to cache: {}",
                    normalizedType, e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((List<CmsOnlineCourseResponse>) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }

    private String normalizeCourseType(String type) {
        if (type == null || type.isBlank()) {
            return "GROUP";
        }
        if ("1-1".equalsIgnoreCase(type)) {
            return "1-1";
        }
        return type.toUpperCase();
    }

    // -----------------------------------------------------------------------
    // Timetable Snapshot - GET /api/erp/students/courses-timetable-emails
    // -----------------------------------------------------------------------

    public CmsEnvelope<List<CmsCourseTimetableResponse>> getCoursesTimetableEmails() {
        try {
            CmsApiResponse<List<CmsCourseTimetableResponse>> resp = cmsWebClient.get()
                    .uri("/api/erp/students/courses-timetable-emails")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<CmsCourseTimetableResponse>>>() {
                    })
                    .block();

            List<CmsCourseTimetableResponse> data = resp != null && resp.data() != null ? resp.data() : List.of();
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getCoursesTimetableEmails failed: {}", e.getMessage());
            return CmsEnvelope.cmsUnavailable();
        }
    }
}
