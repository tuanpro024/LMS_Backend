package com.lms.onllearning.client;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import com.lms.onllearning.dto.response.TimetableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper quanh CMS WebClient.
 * Mọi call đều có fallback: trả data từ Redis cache nếu CMS down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsClient {

    private final WebClient cmsWebClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_SYLLABUS_LIST   = "syllabus:list:all";
    private static final String CACHE_KEY_SYLLABUS_DETAIL = "syllabus:detail:";
    private static final String CACHE_KEY_TIMETABLE       = "timetable:";
    private static final long   SYLLABUS_TTL_SEC          = 600L;
    private static final long   TIMETABLE_TTL_SEC         = 90L;

    // ---------------------------------------------------------------------------
    // Syllabus
    // ---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<SyllabusResponse>> getSyllabuses() {
        String cacheKey = CACHE_KEY_SYLLABUS_LIST;
        try {
            List<SyllabusResponse> data = cmsWebClient.get()
                    .uri("/api/v1/syllabuses")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SyllabusResponse>>() {})
                    .block();

            redisTemplate.opsForValue().set(cacheKey, data, SYLLABUS_TTL_SEC, TimeUnit.SECONDS);
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

    @SuppressWarnings("unchecked")
    public CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId) {
        String cacheKey = CACHE_KEY_SYLLABUS_DETAIL + syllabusId;
        try {
            SyllabusDetailResponse data = cmsWebClient.get()
                    .uri("/api/v1/syllabuses/{id}", syllabusId)
                    .retrieve()
                    .bodyToMono(SyllabusDetailResponse.class)
                    .block();

            redisTemplate.opsForValue().set(cacheKey, data, SYLLABUS_TTL_SEC, TimeUnit.SECONDS);
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

    // ---------------------------------------------------------------------------
    // Timetable
    // ---------------------------------------------------------------------------

    /**
     * Lấy thời khóa biểu của học viên từ CMS.
     * CMS trả 404 → học viên chưa có lịch (noSchedule).
     * CMS down → fallback cache.
     */
    @SuppressWarnings("unchecked")
    public CmsEnvelope<List<TimetableResponse>> getTimetable(String userId) {
        String cacheKey = CACHE_KEY_TIMETABLE + userId;
        try {
            List<TimetableResponse> data = cmsWebClient.get()
                    .uri("/api/v1/students/{studentId}/timetable", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode().value() == 404) {
                            // Học viên chưa được xếp lịch — không phải lỗi
                            return response.createException().map(ex -> new ScheduleNotFoundException());
                        }
                        return response.createException();
                    })
                    .bodyToMono(new ParameterizedTypeReference<List<TimetableResponse>>() {})
                    .block();

            redisTemplate.opsForValue().set(cacheKey, data, TIMETABLE_TTL_SEC, TimeUnit.SECONDS);
            return CmsEnvelope.fromCms(data);

        } catch (ScheduleNotFoundException e) {
            // CMS đã phản hồi 404 → học viên thực sự chưa có lịch
            return CmsEnvelope.noSchedule(List.of());

        } catch (Exception e) {
            log.warn("CMS getTimetable({}) failed, falling back to cache: {}", userId, e.getMessage());
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return CmsEnvelope.fromCache((List<TimetableResponse>) cached);
            }
            return CmsEnvelope.cmsUnavailable();
        }
    }

    /** Sentinel exception khi CMS trả 404 cho timetable */
    private static class ScheduleNotFoundException extends RuntimeException {}
}
