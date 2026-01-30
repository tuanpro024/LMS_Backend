package com.lms.learningpath.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with repo-flashcard service.
 * Fetches StudySet data from the flashcard module.
 */
@FeignClient(name = "repo-flashcard", configuration = com.lms.learningpath.config.FeignConfig.class)
public interface FlashcardServiceClient {

    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetResponse> getStudySetById(@PathVariable("id") String id);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> getAllStudySets();

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> searchStudySets(@RequestParam("q") String query);
}
