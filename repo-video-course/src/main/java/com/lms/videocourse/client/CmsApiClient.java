package com.lms.videocourse.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.dto.cms.CmsApiResponse;
import com.lms.videocourse.dto.cms.CmsCourseDTO;
import com.lms.videocourse.dto.cms.CmsPackageDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * REST client for the external CMS API.
 * Uses RestTemplate (not Feign) because CMS is an external service not registered with Eureka.
 */
@Slf4j
@Component
public class CmsApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CmsApiClient(
            RestTemplateBuilder builder,
            @Value("${app.cms.base-url:http://localhost:1337/api/erp}") String baseUrl,
            @Value("${app.cms.connect-timeout:5000}") int connectTimeout,
            @Value("${app.cms.read-timeout:10000}") int readTimeout) {
        this.baseUrl = baseUrl;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    /**
     * Fetch list of VIDEO courses from CMS.
     */
    public List<CmsCourseDTO> fetchVideoCourses() {
        String url = baseUrl + "/courses/list?type=VIDEO";
        log.info("[CMS] Fetching video courses from: {}", url);
        try {
            ResponseEntity<CmsApiResponse<List<CmsCourseDTO>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {}
            );
            CmsApiResponse<List<CmsCourseDTO>> body = response.getBody();
            if (body != null && body.isSuccess() && body.getData() != null) {
                log.info("[CMS] Fetched {} courses", body.getData().size());
                return body.getData();
            }
            log.warn("[CMS] Courses API returned unsuccessful or null body");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[CMS] Failed to fetch courses: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch CMS courses", e);
        }
    }

    /**
     * Fetch package (syllabus) detail by syllabus ID from CMS.
     */
    public CmsPackageDetailDTO fetchPackageDetail(String syllabusId) {
        String url = baseUrl + "/packages/" + syllabusId;
        log.info("[CMS] Fetching package detail for syllabus: {}", syllabusId);
        try {
            ResponseEntity<CmsPackageDetailDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {}
            );
            CmsPackageDetailDTO body = response.getBody();
            if (body != null) {
                log.info("[CMS] Fetched package '{}' with {} folders",
                        body.getName(),
                        body.getFolders() != null ? body.getFolders().size() : 0);
            }
            return body;
        } catch (Exception e) {
            log.error("[CMS] Failed to fetch package detail for {}: {}", syllabusId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch CMS package: " + syllabusId, e);
        }
    }
}
