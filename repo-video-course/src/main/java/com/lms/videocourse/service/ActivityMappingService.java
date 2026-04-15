package com.lms.videocourse.service;

import com.lms.content.common.delegate.api.*;
import com.lms.content.common.dto.request.*;
import com.lms.content.common.dto.response.*;
import com.lms.content.common.entity.TypeName;
import com.lms.videocourse.dto.request.ActivityMappingRequest;
import com.lms.videocourse.dto.response.ActivityMappingDTO;
import com.lms.videocourse.dto.response.GenerationStatusDTO;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final VideoStepRepository videoStepRepository;
    private final VideoModuleRepository videoModuleRepository;
    
    private final PackageApiDelegate packageDelegate;
    private final FolderApiDelegate folderDelegate;
    private final StudySetApiDelegate studySetDelegate;

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

    @Transactional
    public String generateCourse(String cmsCourseId, String userId) {
        log.info(">>> START course generation for CMS course: {} by user: {}", cmsCourseId, userId);
        
        GenerationStatusDTO status = getGenerationStatus(cmsCourseId);
        log.info("Current generation status: mapped={}/total={}, canGenerate={}", 
                status.getMappedActivities(), status.getTotalActivities(), status.isCanGenerate());
        
        if (!status.isCanGenerate()) {
            log.error("Course not ready! Unmapped indices: {}", status.getUnmappedActivities());
            throw new RuntimeException("Course is not ready for generation. Unmapped activities: " + status.getUnmappedActivities());
        }

        CmsVideoCourse cmsCourse = cmsCourseRepository.findByCmsCourseId(cmsCourseId)
                .orElseThrow(() -> new RuntimeException("CMS Course not found: " + cmsCourseId));

        SyllabusPackage syllabusPkg = packageRepository.findByCmsSyllabusIdAndCmsCourseId(cmsCourse.getSyllabusId(), cmsCourseId)
                .orElseThrow(() -> new RuntimeException("Syllabus Package not found for syllabus: " + cmsCourse.getSyllabusId()));

        log.info("Step 1: Creating internal Package...");
        // 1. Create/Update internal Package
        CreatePackageRequest pkgReq = CreatePackageRequest.builder()
                .name(cmsCourse.getName())
                .description(syllabusPkg.getDescription())
                .type(TypeName.VIDEO_COURSE)
                .category(mapCategory(syllabusPkg.getCategory()))
                .price(cmsCourse.getPrice() != null ? cmsCourse.getPrice() : java.math.BigDecimal.ZERO)
                .build();
        
        PackageResponse pkgResp = packageDelegate.createPackage(pkgReq, userId);
        String packageId = pkgResp.getId();
        log.info("Internal Package created with ID: {}", packageId);

        // 2. Iterate Folders
        List<SyllabusFolder> syllabusFolders = folderRepository.findAllBySyllabusPackageIdAndDeletedFalse(syllabusPkg.getId());
        log.info("Processing {} syllabus folders...", syllabusFolders.size());
        
        for (SyllabusFolder sFolder : syllabusFolders) {
            log.info("  > Creating folder: {}", sFolder.getName());
            CreateFolderRequest folderReq = CreateFolderRequest.builder()
                    .packageId(packageId)
                    .name(sFolder.getName())
                    .description(sFolder.getDescription())
                    .isPrivate(false)
                    .build();
            FolderResponse folderResp = folderDelegate.createFolder(folderReq, userId);
            String folderId = folderResp.getId();

            // 3. Iterate StudySets
            List<SyllabusStudySet> syllabusStudySets = studySetRepository.findAllBySyllabusFolderIdAndDeletedFalse(sFolder.getId());
            log.info("    >> Processing {} study sets...", syllabusStudySets.size());

            for (SyllabusStudySet sSet : syllabusStudySets) {
                log.info("      >>> Creating study set: {}", sSet.getName());
                CreateStudySetRequest ssReq = CreateStudySetRequest.builder()
                        .folderId(folderId)
                        .title(sSet.getName())
                        .description(sSet.getDescription())
                        .isPrivate(false)
                        .build();
                StudySetResponse ssResp = studySetDelegate.createStudySet(ssReq, userId);
                String internalStudySetId = ssResp.getId();

                // 4. Iterate Steps
                List<SyllabusStep> syllabusSteps = stepRepository.findAllByDeletedFalse().stream()
                        .filter(st -> sSet.getId().equals(st.getSyllabusStudySetId()))
                        .sorted(Comparator.comparing(SyllabusStep::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

                for (SyllabusStep sStep : syllabusSteps) {
                    log.info("        >>>> Creating video step: {}", sStep.getName());
                    VideoStep vStep = VideoStep.builder()
                            .studySetId(internalStudySetId)
                            .title(sStep.getName())
                            .description(sStep.getDescription())
                            .stepOrder(sStep.getDisplayOrder())
                            .isRequired(true)
                            .isActive(true)
                            .build();
                    vStep = videoStepRepository.save(vStep);

                    // 5. Iterate Activities -> VideoModules
                    List<SyllabusActivity> syllabusActivities = activityRepository.findAllBySyllabusStepIdAndDeletedFalse(sStep.getId());
                    
                    for (SyllabusActivity sActivity : syllabusActivities) {
                        try {
                            SyllabusActivityModuleMapping mapping = mappingRepository.findBySyllabusActivityIdAndDeletedFalse(sActivity.getId())
                                    .orElseThrow(() -> new RuntimeException("Mapping missing for activity: " + sActivity.getName()));

                            log.info("          >>>>> Adding module: {} type: {}", sActivity.getName(), mapping.getTargetModuleType());
                            
                            VideoModule vModule = VideoModule.builder()
                                    .stepId(vStep.getId())
                                    .title(sActivity.getName())
                                    .description(sActivity.getDescription())
                                    .moduleOrder(sActivity.getDisplayOrder())
                                    .moduleType(com.lms.videocourse.entity.enums.ModuleType.valueOf(mapping.getTargetModuleType().toUpperCase()))
                                    .contentSetId(mapping.getTargetContentSetId())
                                    .videoCode(mapping.getTargetModuleType().equalsIgnoreCase("VIDEO") ? mapping.getTargetContentSetId() : null)
                                    .isRequired(mapping.isRequired())
                                    .isActive(true)
                                    .build();
                            
                            videoModuleRepository.save(vModule);
                        } catch (Exception ex) {
                            log.error("Error creating module for activity {}: {}", sActivity.getName(), ex.getMessage());
                            throw ex; 
                        }
                    }
                }
            }
        }

        log.info("<<< Course generation COMPLETED successfully for: {}. Package ID: {}", cmsCourseId, packageId);
        return packageId;
    }

    private com.lms.content.common.entity.CategoryType mapCategory(String syllabusCategory) { 
        if (syllabusCategory == null) return com.lms.content.common.entity.CategoryType.HSK;
        log.info("Mapping category string: {}", syllabusCategory);
        try {
            // Basic mapping logic
            String cat = syllabusCategory.toUpperCase();
            if (cat.contains("HSK")) return com.lms.content.common.entity.CategoryType.HSK;
            if (cat.contains("BUSINESS")) return com.lms.content.common.entity.CategoryType.BUSINESS_CHINESE;
            if (cat.contains("FREE")) return com.lms.content.common.entity.CategoryType.FREE_LEARNING;
            
            return com.lms.content.common.entity.CategoryType.valueOf(cat);
        } catch (Exception e) {
            log.warn("Unknown category: {}, defaulting to HSK", syllabusCategory);
            return com.lms.content.common.entity.CategoryType.HSK;
        }
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
