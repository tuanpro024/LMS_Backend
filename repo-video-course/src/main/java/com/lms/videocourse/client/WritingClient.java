package com.lms.videocourse.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.videocourse.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for repo-writing service.
 * Used by AvailableModuleService to list writing study sets.
 */
@FeignClient(name = "repo-writing", contextId = "vc-writing", configuration = FeignConfig.class)
public interface WritingClient {

    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetResponse> getStudySetById(@PathVariable("id") String id);

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> getAllStudySets();

    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> searchStudySets(@RequestParam("q") String query);

    @GetMapping("/packages")
    ApiResponse<List<PackageResponse>> getPackagesByType(@RequestParam("type") TypeName type);

    @GetMapping("/study-sets/folder/{folderId}")
    ApiResponse<List<StudySetResponse>> getStudySetsByFolderId(@PathVariable("folderId") String folderId);
}
