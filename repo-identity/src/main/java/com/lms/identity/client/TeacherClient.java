package com.lms.identity.client;

import com.lms.identity.dto.response.CmsApiResponse;
import com.lms.identity.dto.response.CmsTeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeacherClient {

    private final WebClient cmsWebClient;

    public List<CmsTeacherResponse> getTeachers() {
        try {
            CmsApiResponse<List<CmsTeacherResponse>> response = cmsWebClient.get()
                    .uri("/api/erp/teachers/list")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CmsApiResponse<List<CmsTeacherResponse>>>() {})
                    .block();

            if (response != null && response.data() != null) {
                return response.data();
            }
        } catch (Exception e) {
            log.error("Error fetching teachers from CMS: {}", e.getMessage());
        }
        return List.of();
    }
}
