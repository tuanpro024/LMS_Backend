package com.lms.videocourse.service.helper;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.videocourse.dto.response.AvailableModuleResponse;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.content.common.entity.enums.PublishStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Helper to fetch study sets that belong to VIDEO_COURSE packages only.
 * Uses functional interfaces so the same logic works across all 6 service clients.
 */
@Component
@Slf4j
public class VideoCourseStudySetFetcher {

    /**
     * Fetch study sets from VIDEO_COURSE packages of a specific service.
     *
     * @param packagesSupplier supplier that calls getPackagesByType(VIDEO_COURSE) on the client
     * @param studySetsFetcher function that calls getStudySetsByFolderId(folderId) on the client
     * @param moduleType       the module type (FLASHCARD, WRITING, etc.)
     * @param repoName         repository name for logging
     * @param query            optional search query to filter by title/description
     * @return list of AvailableModuleResponse, deduplicated and optionally filtered
     */
    public List<AvailableModuleResponse> fetch(
            Supplier<ApiResponse<List<PackageResponse>>> packagesSupplier,
            Function<String, ApiResponse<List<StudySetResponse>>> studySetsFetcher,
            ModuleType moduleType,
            String repoName,
            String query) {

        try {
            // Step 1: Get VIDEO_COURSE packages
            ApiResponse<List<PackageResponse>> packagesResponse = packagesSupplier.get();
            List<PackageResponse> packages = packagesResponse.data();
            if (packages == null || packages.isEmpty()) {
                log.debug("[{}] No VIDEO_COURSE packages found", repoName);
                return new ArrayList<>();
            }

            // Filter to only PUBLISHED packages (exclude DRAFT)
            packages = packages.stream()
                    .filter(pkg -> pkg.getPublishStatus() == PublishStatus.PUBLISHED)
                    .collect(Collectors.toList());

            if (packages.isEmpty()) {
                log.debug("[{}] No PUBLISHED VIDEO_COURSE packages found", repoName);
                return new ArrayList<>();
            }

            log.debug("[{}] Found {} PUBLISHED VIDEO_COURSE packages", repoName, packages.size());

            // Step 2: Extract folders from packages
            // Map folderId -> folderName for later use
            Map<String, String> folderIdToName = new LinkedHashMap<>();
            for (PackageResponse pkg : packages) {
                if (pkg.getFolders() != null) {
                    for (FolderResponse folder : pkg.getFolders()) {
                        folderIdToName.putIfAbsent(folder.getId(), folder.getName());
                    }
                }
            }

            if (folderIdToName.isEmpty()) {
                log.debug("[{}] No folders found in VIDEO_COURSE packages", repoName);
                return new ArrayList<>();
            }

            log.debug("[{}] Found {} unique folders across VIDEO_COURSE packages", repoName, folderIdToName.size());

            // Step 3: Fetch study sets for each folder
            // Use a map to deduplicate by studySetId (keep first occurrence)
            Map<String, AvailableModuleResponse> resultMap = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : folderIdToName.entrySet()) {
                String folderId = entry.getKey();
                String folderName = entry.getValue();

                try {
                    ApiResponse<List<StudySetResponse>> ssResponse = studySetsFetcher.apply(folderId);
                    List<StudySetResponse> studySets = ssResponse.data();
                    if (studySets != null) {
                        for (StudySetResponse ss : studySets) {
                            resultMap.putIfAbsent(ss.getId(), toResponse(ss, moduleType, repoName, folderId, folderName));
                        }
                    }
                } catch (Exception e) {
                    log.warn("[{}] Failed to fetch study sets for folder {}: {}", repoName, folderId, e.getMessage());
                    // Continue with other folders
                }
            }

            log.debug("[{}] Total unique study sets from VIDEO_COURSE: {}", repoName, resultMap.size());

            // Step 4: Filter by query if present
            List<AvailableModuleResponse> results = new ArrayList<>(resultMap.values());
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                results = results.stream()
                        .filter(r -> (r.getTitle() != null && r.getTitle().toLowerCase().contains(q))
                                || (r.getDescription() != null && r.getDescription().toLowerCase().contains(q)))
                        .collect(Collectors.toList());
            }

            return results;

        } catch (Exception e) {
            log.error("[{}] Error fetching VIDEO_COURSE study sets: {}", repoName, e.getMessage());
            return new ArrayList<>();
        }
    }

    private AvailableModuleResponse toResponse(
            StudySetResponse ss, ModuleType moduleType, String repoName,
            String folderId, String folderName) {
        return AvailableModuleResponse.builder()
                .id(ss.getId())
                .title(ss.getTitle())
                .description(ss.getDescription())
                .thumbnail(ss.getThumbnail())
                .moduleType(moduleType)
                .repoName(repoName)
                .folderId(folderId)
                .folderName(folderName)
                .itemCount((long) ss.getTotalItems())
                .isPrivate(ss.isPrivate())
                .userId(ss.getUserId())
                .build();
    }
}
