package com.lms.videocourse.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.videocourse.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for repo-quiz service.
 * Used by AvailableModuleService to list quiz study sets.
 */
@FeignClient(name = "repo-quiz", contextId = "vc-quiz", configuration = FeignConfig.class)
public interface QuizClient {

    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetResponse> getStudySetById(@PathVariable("id") String id);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> getAllStudySets(@RequestParam("packageType") String packageType);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> searchStudySets(@RequestParam("q") String query, @RequestParam("packageType") String packageType);
}
