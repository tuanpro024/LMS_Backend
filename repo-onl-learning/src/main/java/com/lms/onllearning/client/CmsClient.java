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

import java.time.LocalDate;
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

    private static final String CACHE_KEY_TIMETABLE = "timetable:student:";

    @Value("${cache.ttl.timetable:90}")
    private long timetableTtlSec;

    // -----------------------------------------------------------------------
    // Syllabus List — GET /api/erp/syllabus
    // -----------------------------------------------------------------------

    public CmsEnvelope<List<SyllabusResponse>> getSyllabuses() {
        try {
            CmsApiResponse<List<SyllabusResponse>> resp = cmsWebClient.get()
                    .uri("/api/erp/syllabus")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<SyllabusResponse>>>() {})
                    .block();

            List<SyllabusResponse> data = resp != null ? resp.data() : List.of();
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getSyllabuses failed: {}", e.getMessage());
            return CmsEnvelope.cmsUnavailable();
        }
    }

    // -----------------------------------------------------------------------
    // Syllabus Detail — GET /api/erp/syllabus/{id}
    // -----------------------------------------------------------------------

    public CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId) {
        try {
            CmsApiResponse<SyllabusDetailResponse> resp = cmsWebClient.get()
                    .uri("/api/erp/syllabus/{id}", syllabusId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<SyllabusDetailResponse>>() {})
                    .block();

            SyllabusDetailResponse data = resp != null ? resp.data() : null;
            return CmsEnvelope.fromCms(data);

        } catch (Exception e) {
            log.warn("CMS getSyllabusDetail({}) failed: {}", syllabusId, e.getMessage());
            return CmsEnvelope.cmsUnavailable();
        }
    }

    // -----------------------------------------------------------------------
    // Student Timetable — GET /api/erp/students/{studentId}/timetable
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(
            String studentId,
            LocalDate start,
            LocalDate end) {

        String cacheKey = CACHE_KEY_TIMETABLE + studentId + ":" + start + ":" + end;
        try {
            CmsApiResponse<List<StudentTimetableItemResponse>> resp = cmsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/erp/students/{studentId}/timetable")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .build(studentId))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<StudentTimetableItemResponse>>>() {})
                    .block();

            List<StudentTimetableItemResponse> data = resp != null && resp.data() != null ? resp.data() : List.of();
            try {
                redisTemplate.opsForValue().set(cacheKey, data, timetableTtlSec, TimeUnit.SECONDS);
            } catch (Exception cacheEx) {
                log.warn("CMS getStudentTimetable({}, {} -> {}): cache write failed, continue with CMS data: {}",
                        studentId, start, end, cacheEx.getMessage());
            }
            return CmsEnvelope.fromCms(data);

        } catch (WebClientResponseException.NotFound notFound) {
            log.info("CMS getStudentTimetable({}, {} -> {}): student has no schedule (404)", studentId, start, end);
            return CmsEnvelope.noSchedule(List.of());

        } catch (Exception e) {
            log.warn("CMS getStudentTimetable({}, {} -> {}) failed, falling back to cache: {}",
                    studentId, start, end, e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((List<StudentTimetableItemResponse>) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }
}
