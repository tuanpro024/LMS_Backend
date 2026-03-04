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
 * Feign client for repo-flashcard service.
 * Used by AvailableModuleService to list flashcard study sets.
 */
@FeignClient(name = "repo-flashcard", contextId = "vc-flashcard", configuration = FeignConfig.class)
public interface FlashcardClient {

    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetResponse> getStudySetById(@PathVariable("id") String id);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> getAllStudySets();

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> searchStudySets(@RequestParam("q") String query);
}
