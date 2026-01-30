package com.lms.learningpath.client;

import com.lms.common.dto.ApiResponse;
import com.lms.learningpath.dto.external.StudySetDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with repo-kanji-origin service.
 * Fetches StudySet data from the kanji-origin module.
 */
@FeignClient(name = "repo-kanji-origin")
public interface KanjiOriginServiceClient {

    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetDto> getStudySetById(@PathVariable("id") String id);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetDto>> getAllStudySets();

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetDto>> searchStudySets(@RequestParam("q") String query);
}
