package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.ActivityMappingRequest;
import com.lms.videocourse.dto.response.ActivityMappingDTO;
import com.lms.videocourse.dto.response.GenerationStatusDTO;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityMappingService {

    private final SyllabusActivityModuleMappingRepository mappingRepository;
    private final SyllabusActivityRepository activityRepository;
    private final SyllabusStepRepository stepRepository;
    private final SyllabusStudySetRepository studySetRepository;
    private final SyllabusFolderRepository folderRepository;
    private final SyllabusPackageRepository packageRepository;
    private final CmsVideoCourseRepository cmsCourseRepository;

    /**
     * Get mapping for a specific activity.
     */
    public ActivityMappingDTO getMappingByActivityId(String activityId) {
        return mappingRepository.findBySyllabusActivityIdAndDeletedFalse(activityId)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Get all mappings for a list of activity IDs.
     */
    public Map<String, ActivityMappingDTO> getMappingsByActivityIds(List<String> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) return Map.of();
        return mappingRepository.findAllBySyllabusActivityIdInAndDeletedFalse(activityIds)
                .stream()
                .collect(Collectors.toMap(
                        SyllabusActivityModuleMapping::getSyllabusActivityId,
                        this::toDTO,
                        (a, b) -> a
                ));
    }

    /**
     * Create or update mapping for a specific activity.
     */
    public ActivityMappingDTO saveMapping(String activityId, ActivityMappingRequest request, String userId) {
        // Validate activity exists
        activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));

        SyllabusActivityModuleMapping mapping = mappingRepository
                .findBySyllabusActivityIdAndDeletedFalse(activityId)
                .orElse(SyllabusActivityModuleMapping.builder()
                        .syllabusActivityId(activityId)
                        .build());

        mapping.setTargetModuleType(request.getTargetModuleType());
        mapping.setTargetContentSetId(request.getTargetContentSetId());
        mapping.setTargetContentFolderId(request.getTargetContentFolderId());
        mapping.setTargetPackageId(request.getTargetPackageId());
        mapping.setTargetModuleTitle(request.getTargetModuleTitle());
        mapping.setTargetModuleDescription(request.getTargetModuleDescription());
        mapping.setRequired(request.isRequired());
        mapping.setStatus("MAPPED");
        mapping.setCreatedBy(userId);
        mapping.setDeleted(false);

        mappingRepository.save(mapping);
        log.info("[Mapping] Saved mapping for activity {} -> {} ({})",
                activityId, request.getTargetModuleType(), request.getTargetContentSetId());

        return toDTO(mapping);
    }

    /**
     * Delete mapping for a specific activity.
     */
    public void deleteMapping(String activityId) {
        mappingRepository.findBySyllabusActivityIdAndDeletedFalse(activityId)
                .ifPresent(mapping -> {
                    mapping.setDeleted(true);
                    mappingRepository.save(mapping);
                    log.info("[Mapping] Soft-deleted mapping for activity {}", activityId);
                });
    }

    /**
     * Check generation readiness for a course.
     * Returns total/mapped/unmapped activities and whether all are mapped.
     */
    public GenerationStatusDTO getGenerationStatus(String cmsCourseId) {
        CmsVideoCourse course = cmsCourseRepository.findByCmsCourseId(cmsCourseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + cmsCourseId));

        // Find the package
        SyllabusPackage pkg = packageRepository
                .findByCmsSyllabusIdAndCmsCourseId(course.getSyllabusId(), course.getCmsCourseId())
                .orElse(null);

        if (pkg == null) {
            return GenerationStatusDTO.builder()
                    .cmsCourseId(cmsCourseId)
                    .courseName(course.getName())
                    .totalActivities(0)
                    .mappedActivities(0)
                    .unmappedActivities(0)
                    .canGenerate(false)
                    .unmappedList(List.of())
                    .build();
        }

        // Traverse the tree to collect all activities
        List<SyllabusFolder> folders = folderRepository.findAllBySyllabusPackageIdAndDeletedFalse(pkg.getId());
        List<String> folderIds = folders.stream().map(SyllabusFolder::getId).toList();
        Map<String, String> folderNameMap = folders.stream().collect(Collectors.toMap(SyllabusFolder::getId, SyllabusFolder::getName));

        List<SyllabusStudySet> studySets = studySetRepository.findAllByDeletedFalse().stream()
                .filter(ss -> folderIds.contains(ss.getSyllabusFolderId()))
                .toList();
        List<String> ssIds = studySets.stream().map(SyllabusStudySet::getId).toList();
        Map<String, String> ssNameMap = studySets.stream().collect(Collectors.toMap(SyllabusStudySet::getId, SyllabusStudySet::getName));
        Map<String, String> ssFolderMap = studySets.stream().collect(Collectors.toMap(SyllabusStudySet::getId, SyllabusStudySet::getSyllabusFolderId));

        List<SyllabusStep> steps = stepRepository.findAllByDeletedFalse().stream()
                .filter(s -> ssIds.contains(s.getSyllabusStudySetId()))
                .toList();
        List<String> stepIds = steps.stream().map(SyllabusStep::getId).toList();
        Map<String, String> stepNameMap = steps.stream().collect(Collectors.toMap(SyllabusStep::getId, SyllabusStep::getName));
        Map<String, String> stepSSMap = steps.stream().collect(Collectors.toMap(SyllabusStep::getId, SyllabusStep::getSyllabusStudySetId));

        List<SyllabusActivity> activities = activityRepository.findAllBySyllabusStepIdInAndDeletedFalse(stepIds);
        List<String> activityIds = activities.stream().map(SyllabusActivity::getId).toList();

        // Get existing mappings
        Set<String> mappedActivityIds = mappingRepository
                .findAllBySyllabusActivityIdInAndDeletedFalse(activityIds)
                .stream()
                .map(SyllabusActivityModuleMapping::getSyllabusActivityId)
                .collect(Collectors.toSet());

        // Build unmapped list
        List<GenerationStatusDTO.UnmappedActivityInfo> unmappedList = activities.stream()
                .filter(a -> !mappedActivityIds.contains(a.getId()))
                .map(a -> {
                    String stepName = stepNameMap.getOrDefault(a.getSyllabusStepId(), "");
                    String ssId = stepSSMap.getOrDefault(a.getSyllabusStepId(), "");
                    String ssName = ssNameMap.getOrDefault(ssId, "");
                    String folderId = ssFolderMap.getOrDefault(ssId, "");
                    String folderName = folderNameMap.getOrDefault(folderId, "");

                    return GenerationStatusDTO.UnmappedActivityInfo.builder()
                            .activityId(a.getId())
                            .activityName(a.getName())
                            .stepName(stepName)
                            .studySetName(ssName)
                            .folderName(folderName)
                            .build();
                })
                .toList();

        int total = activities.size();
        int mapped = mappedActivityIds.size();

        return GenerationStatusDTO.builder()
                .cmsCourseId(cmsCourseId)
                .courseName(course.getName())
                .totalActivities(total)
                .mappedActivities(mapped)
                .unmappedActivities(total - mapped)
                .canGenerate(total > 0 && mapped == total)
                .unmappedList(unmappedList)
                .build();
    }

    private ActivityMappingDTO toDTO(SyllabusActivityModuleMapping entity) {
        return ActivityMappingDTO.builder()
                .id(entity.getId())
                .syllabusActivityId(entity.getSyllabusActivityId())
                .targetModuleType(entity.getTargetModuleType())
                .targetContentSetId(entity.getTargetContentSetId())
                .targetContentFolderId(entity.getTargetContentFolderId())
                .targetPackageId(entity.getTargetPackageId())
                .targetModuleTitle(entity.getTargetModuleTitle())
                .targetModuleDescription(entity.getTargetModuleDescription())
                .isRequired(entity.isRequired())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
