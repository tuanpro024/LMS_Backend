package com.lms.onllearning.client;

import com.lms.onllearning.dto.response.CmsApiResponse;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client gọi CMS API: https://cms.dangch.tech
 * CMS trả format: {"statusCode":200,"data":...,"message":...,"success":true}
 * Mọi call đều có fallback: trả data từ Redis cache nếu CMS down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsClient {

    private final WebClient cmsWebClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_SYLLABUS_LIST = "syllabus:list:all";
    private static final String CACHE_KEY_SYLLABUS_DETAIL = "syllabus:detail:";
    private static final String CACHE_KEY_TIMETABLE = "timetable:student:";
    private static final long SYLLABUS_TTL_SEC = 600L;

    @Value("${cache.ttl.timetable:90}")
    private long timetableTtlSec;

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
    // Student Timetable — GET /api/erp/students/timetable-by-email?email={email}
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(String email) {

        String cacheKey = CACHE_KEY_TIMETABLE + email;
        try {
            CmsApiResponse<List<StudentTimetableItemResponse>> resp = cmsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/erp/students/timetable-by-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<StudentTimetableItemResponse>>>() {
                    })
                    .block();

            List<StudentTimetableItemResponse> data = resp != null && resp.data() != null ? resp.data() : List.of();
            try {
                redisTemplate.opsForValue().set(cacheKey, data, timetableTtlSec, TimeUnit.SECONDS);
            } catch (Exception cacheEx) {
                log.warn("CMS getStudentTimetable(email={}): cache write failed, continue with CMS data: {}",
                        email, cacheEx.getMessage());
            }
            return CmsEnvelope.fromCms(data);

        } catch (WebClientResponseException.NotFound notFound) {
            log.info("CMS getStudentTimetable(email={}): student has no schedule (404)", email);
            return CmsEnvelope.noSchedule(List.of());

        } catch (Exception e) {
            log.warn("CMS getStudentTimetable(email={}) failed, falling back to cache: {}",
                    email, e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((List<StudentTimetableItemResponse>) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }
}
