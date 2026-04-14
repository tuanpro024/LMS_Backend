package com.lms.videocourse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.videocourse.client.CmsApiClient;
import com.lms.videocourse.dto.cms.CmsCourseDTO;
import com.lms.videocourse.dto.cms.CmsPackageDetailDTO;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.entity.enums.SyncStatus;
import com.lms.videocourse.entity.enums.SyncTriggerType;
import com.lms.videocourse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Service responsible for synchronizing syllabus data from the external CMS
 * into the local database using a delta-upsert strategy based on payload hashing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyllabusSyncService {

    private final CmsApiClient cmsApiClient;
    private final CmsVideoCourseRepository cmsCourseRepository;
    private final SyllabusPackageRepository packageRepository;
    private final SyllabusFolderRepository folderRepository;
    private final SyllabusStudySetRepository studySetRepository;
    private final SyllabusStepRepository stepRepository;
    private final SyllabusActivityRepository activityRepository;
    private final SyllabusSyncRunRepository syncRunRepository;
    private final ObjectMapper objectMapper;

    /** Simple single-instance lock to prevent concurrent syncs */
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    /**
     * Trigger a full sync of all CMS video courses and their syllabus data.
     * Returns the sync run record for tracking.
     */
    public SyllabusSyncRun syncAll(SyncTriggerType triggerType) {
        if (!syncInProgress.compareAndSet(false, true)) {
            log.warn("[Sync] Sync already in progress, skipping");
            throw new IllegalStateException("A sync operation is already in progress");
        }

        SyllabusSyncRun run = SyllabusSyncRun.builder()
                .triggerType(triggerType)
                .status(SyncStatus.RUNNING)
                .startedAt(Instant.now())
                .build();
        syncRunRepository.save(run);

        int synced = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try {
            // Step 1: Fetch course list from CMS
            List<CmsCourseDTO> cmsCourses = cmsApiClient.fetchVideoCourses();
            log.info("[Sync] Fetched {} courses from CMS", cmsCourses.size());

            // Step 2: Upsert CMS courses
            Set<String> activeCmsCourseIds = new HashSet<>();
            for (CmsCourseDTO cmsCourse : cmsCourses) {
                upsertCmsVideoCourse(cmsCourse);
                activeCmsCourseIds.add(cmsCourse.getId());
            }

            // Soft-delete courses no longer in CMS
            softDeleteRemovedCourses(activeCmsCourseIds);

            // Step 3: Sync syllabus detail for each course
            for (CmsCourseDTO cmsCourse : cmsCourses) {
                if (cmsCourse.getSyllabusId() == null || cmsCourse.getSyllabusId().isBlank()) {
                    log.debug("[Sync] Skipping course {} - no syllabus_id", cmsCourse.getId());
                    continue;
                }
                try {
                    syncCourseDetail(cmsCourse);
                    synced++;
                } catch (Exception e) {
                    failed++;
                    String msg = String.format("Course %s (%s): %s",
                            cmsCourse.getId(), cmsCourse.getName(), e.getMessage());
                    errors.add(msg);
                    log.error("[Sync] Failed to sync course {}: {}", cmsCourse.getId(), e.getMessage(), e);
                }
            }

            // Update run status
            run.setStatus(failed == 0 ? SyncStatus.SUCCESS : SyncStatus.FAILED);
            run.setSummary(String.format("Synced %d courses, %d failed out of %d total",
                    synced, failed, cmsCourses.size()));
            if (!errors.isEmpty()) {
                run.setErrorMessage(String.join("\n", errors));
            }

        } catch (Exception e) {
            run.setStatus(SyncStatus.FAILED);
            run.setErrorMessage("Fatal sync error: " + e.getMessage());
            log.error("[Sync] Fatal error during sync: {}", e.getMessage(), e);
        } finally {
            run.setFinishedAt(Instant.now());
            syncRunRepository.save(run);
            syncInProgress.set(false);
        }

        return run;
    }

    // ==================== CMS Course Upsert ====================

    private void upsertCmsVideoCourse(CmsCourseDTO dto) {
        Instant now = Instant.now();
        Optional<CmsVideoCourse> existing = cmsCourseRepository.findByCmsCourseId(dto.getId());

        if (existing.isPresent()) {
            CmsVideoCourse entity = existing.get();
            entity.setCode(dto.getCode());
            entity.setName(dto.getName());
            entity.setPrice(parsePrice(dto.getPrice()));
            entity.setCourseType(dto.getType());
            entity.setLevel(dto.getLevel());
            entity.setTotalLessons(dto.getTotalLessons());
            entity.setSyllabusId(dto.getSyllabusId());
            entity.setLastSyncedAt(now);
            entity.setDeleted(false);
            cmsCourseRepository.save(entity);
        } else {
            CmsVideoCourse entity = CmsVideoCourse.builder()
                    .cmsCourseId(dto.getId())
                    .code(dto.getCode())
                    .name(dto.getName())
                    .price(parsePrice(dto.getPrice()))
                    .courseType(dto.getType())
                    .level(dto.getLevel())
                    .totalLessons(dto.getTotalLessons())
                    .syllabusId(dto.getSyllabusId())
                    .lastSyncedAt(now)
                    .build();
            cmsCourseRepository.save(entity);
        }
    }

    private void softDeleteRemovedCourses(Set<String> activeCmsCourseIds) {
        List<CmsVideoCourse> allActive = cmsCourseRepository.findAllByDeletedFalse();
        for (CmsVideoCourse course : allActive) {
            if (!activeCmsCourseIds.contains(course.getCmsCourseId())) {
                course.setDeleted(true);
                cmsCourseRepository.save(course);
                log.info("[Sync] Soft-deleted course: {} ({})", course.getCmsCourseId(), course.getName());
            }
        }
    }

    // ==================== Course Detail Sync ====================

    @Transactional
    public void syncCourseDetail(CmsCourseDTO cmsCourse) {
        CmsPackageDetailDTO packageDetail = cmsApiClient.fetchPackageDetail(cmsCourse.getSyllabusId());
        if (packageDetail == null) {
            throw new RuntimeException("CMS returned null for package: " + cmsCourse.getSyllabusId());
        }

        Instant now = Instant.now();

        // Upsert SyllabusPackage
        SyllabusPackage pkg = upsertPackage(cmsCourse, packageDetail, now);

        // Track active IDs for soft-delete
        Set<String> activeFolderIds = new HashSet<>();
        Set<String> activeStudySetIds = new HashSet<>();
        Set<String> activeStepIds = new HashSet<>();
        Set<String> activeActivityIds = new HashSet<>();

        if (packageDetail.getFolders() != null) {
            int folderOrder = 0;
            for (CmsPackageDetailDTO.CmsFolder cmsFolder : packageDetail.getFolders()) {
                SyllabusFolder folder = upsertFolder(cmsFolder, pkg.getId(), folderOrder++, now);
                activeFolderIds.add(folder.getId());

                if (cmsFolder.getUnits() != null) {
                    for (CmsPackageDetailDTO.CmsUnit cmsUnit : cmsFolder.getUnits()) {
                        SyllabusStudySet studySet = upsertStudySet(cmsUnit, folder.getId(), now);
                        activeStudySetIds.add(studySet.getId());

                        if (cmsUnit.getVideoLessons() != null) {
                            for (CmsPackageDetailDTO.CmsVideoLesson cmsLesson : cmsUnit.getVideoLessons()) {
                                SyllabusStep step = upsertStep(cmsLesson, studySet.getId(), now);
                                activeStepIds.add(step.getId());

                                if (cmsLesson.getModules() != null) {
                                    int moduleOrder = 0;
                                    for (CmsPackageDetailDTO.CmsModule cmsModule : cmsLesson.getModules()) {
                                        SyllabusActivity activity = upsertActivity(cmsModule, step.getId(), moduleOrder++, now);
                                        activeActivityIds.add(activity.getId());
                                    }
                                }
                                // Soft delete removed activities for this step
                                softDeleteRemovedActivities(step.getId(), activeActivityIds);
                            }
                        }
                        // Soft delete removed steps for this study set
                        softDeleteRemovedSteps(studySet.getId(), activeStepIds);
                    }
                }
                // Soft delete removed study sets for this folder
                softDeleteRemovedStudySets(folder.getId(), activeStudySetIds);
            }
        }
        // Soft delete removed folders for this package
        softDeleteRemovedFolders(pkg.getId(), activeFolderIds);
    }

    // ==================== Upsert Methods ====================

    private SyllabusPackage upsertPackage(CmsCourseDTO course, CmsPackageDetailDTO detail, Instant now) {
        String hash = computeHash(detail.getName(), detail.getDescription(), detail.getCategory());
        Optional<SyllabusPackage> existing = packageRepository
                .findByCmsSyllabusIdAndCmsCourseId(course.getSyllabusId(), course.getId());

        if (existing.isPresent()) {
            SyllabusPackage pkg = existing.get();
            if (!hash.equals(pkg.getSourcePayloadHash())) {
                pkg.setName(detail.getName());
                pkg.setDescription(detail.getDescription());
                pkg.setCategory(detail.getCategory());
                pkg.setCourseName(course.getName());
                pkg.setCourseDescription(null); // CMS doesn't have course description yet
                pkg.setCoursePrice(parsePrice(course.getPrice()));
                pkg.setCourseType(course.getType());
                pkg.setSourcePayloadHash(hash);
                pkg.setLastSyncedAt(now);
                pkg.setDeleted(false);
                return packageRepository.save(pkg);
            }
            pkg.setLastSyncedAt(now);
            pkg.setDeleted(false);
            return packageRepository.save(pkg);
        } else {
            SyllabusPackage pkg = SyllabusPackage.builder()
                    .name(detail.getName())
                    .description(detail.getDescription())
                    .category(detail.getCategory())
                    .cmsSyllabusId(course.getSyllabusId())
                    .cmsCourseId(course.getId())
                    .courseName(course.getName())
                    .coursePrice(parsePrice(course.getPrice()))
                    .courseType(course.getType())
                    .sourcePayloadHash(hash)
                    .lastSyncedAt(now)
                    .build();
            return packageRepository.save(pkg);
        }
    }

    private SyllabusFolder upsertFolder(CmsPackageDetailDTO.CmsFolder cms, String packageId, int order, Instant now) {
        String hash = computeHash(cms.getName(), cms.getDescription());
        Optional<SyllabusFolder> existing = folderRepository.findByCmsFolderId(cms.getId());

        if (existing.isPresent()) {
            SyllabusFolder folder = existing.get();
            if (!hash.equals(folder.getSourcePayloadHash())) {
                folder.setName(cms.getName());
                folder.setDescription(cms.getDescription());
                folder.setDisplayOrder(order);
                folder.setSourcePayloadHash(hash);
                folder.setLastSyncedAt(now);
                folder.setDeleted(false);
                return folderRepository.save(folder);
            }
            folder.setLastSyncedAt(now);
            folder.setDeleted(false);
            return folderRepository.save(folder);
        } else {
            SyllabusFolder folder = SyllabusFolder.builder()
                    .name(cms.getName())
                    .description(cms.getDescription())
                    .syllabusPackageId(packageId)
                    .cmsFolderId(cms.getId())
                    .displayOrder(order)
                    .sourcePayloadHash(hash)
                    .lastSyncedAt(now)
                    .build();
            return folderRepository.save(folder);
        }
    }

    private SyllabusStudySet upsertStudySet(CmsPackageDetailDTO.CmsUnit cms, String folderId, Instant now) {
        String hash = computeHash(cms.getTopic(), cms.getContent(), String.valueOf(cms.getSessionNo()));
        Optional<SyllabusStudySet> existing = studySetRepository.findByCmsUnitId(cms.getId());

        if (existing.isPresent()) {
            SyllabusStudySet ss = existing.get();
            if (!hash.equals(ss.getSourcePayloadHash())) {
                ss.setName(cms.getTopic());
                ss.setDescription(cms.getContent());
                ss.setSessionNo(cms.getSessionNo());
                ss.setSourcePayloadHash(hash);
                ss.setLastSyncedAt(now);
                ss.setDeleted(false);
                return studySetRepository.save(ss);
            }
            ss.setLastSyncedAt(now);
            ss.setDeleted(false);
            return studySetRepository.save(ss);
        } else {
            SyllabusStudySet ss = SyllabusStudySet.builder()
                    .name(cms.getTopic())
                    .description(cms.getContent())
                    .syllabusFolderId(folderId)
                    .cmsUnitId(cms.getId())
                    .sessionNo(cms.getSessionNo())
                    .sourcePayloadHash(hash)
                    .lastSyncedAt(now)
                    .build();
            return studySetRepository.save(ss);
        }
    }

    private SyllabusStep upsertStep(CmsPackageDetailDTO.CmsVideoLesson cms, String studySetId, Instant now) {
        String hash = computeHash(cms.getName(), cms.getDescription(), String.valueOf(cms.getOrderIndex()));
        Optional<SyllabusStep> existing = stepRepository.findByCmsVideoLessonId(cms.getId());

        if (existing.isPresent()) {
            SyllabusStep step = existing.get();
            if (!hash.equals(step.getSourcePayloadHash())) {
                step.setName(cms.getName());
                step.setDescription(cms.getDescription());
                step.setDisplayOrder(cms.getOrderIndex());
                step.setSourcePayloadHash(hash);
                step.setLastSyncedAt(now);
                step.setDeleted(false);
                return stepRepository.save(step);
            }
            step.setLastSyncedAt(now);
            step.setDeleted(false);
            return stepRepository.save(step);
        } else {
            SyllabusStep step = SyllabusStep.builder()
                    .name(cms.getName())
                    .description(cms.getDescription())
                    .syllabusStudySetId(studySetId)
                    .cmsVideoLessonId(cms.getId())
                    .displayOrder(cms.getOrderIndex())
                    .sourcePayloadHash(hash)
                    .lastSyncedAt(now)
                    .build();
            return stepRepository.save(step);
        }
    }

    private SyllabusActivity upsertActivity(CmsPackageDetailDTO.CmsModule cms, String stepId, int order, Instant now) {
        String hash = computeHash(cms.getName(), cms.getVideoContent(), cms.getDocumentContent());
        Optional<SyllabusActivity> existing = activityRepository.findByCmsModuleId(cms.getId());

        if (existing.isPresent()) {
            SyllabusActivity activity = existing.get();
            if (!hash.equals(activity.getSourcePayloadHash())) {
                activity.setName(cms.getName());
                activity.setVideoContent(cms.getVideoContent());
                activity.setDocumentContent(cms.getDocumentContent());
                activity.setDisplayOrder(order);
                activity.setSourcePayloadHash(hash);
                activity.setLastSyncedAt(now);
                activity.setDeleted(false);
                return activityRepository.save(activity);
            }
            activity.setLastSyncedAt(now);
            activity.setDeleted(false);
            return activityRepository.save(activity);
        } else {
            SyllabusActivity activity = SyllabusActivity.builder()
                    .syllabusStepId(stepId)
                    .cmsModuleId(cms.getId())
                    .name(cms.getName())
                    .videoContent(cms.getVideoContent())
                    .documentContent(cms.getDocumentContent())
                    .displayOrder(order)
                    .sourcePayloadHash(hash)
                    .lastSyncedAt(now)
                    .build();
            return activityRepository.save(activity);
        }
    }

    // ==================== Soft Delete ====================

    private void softDeleteRemovedFolders(String packageId, Set<String> activeIds) {
        folderRepository.findAllBySyllabusPackageIdAndDeletedFalse(packageId).stream()
                .filter(f -> !activeIds.contains(f.getId()))
                .forEach(f -> {
                    f.setDeleted(true);
                    folderRepository.save(f);
                    log.info("[Sync] Soft-deleted folder: {}", f.getName());
                });
    }

    private void softDeleteRemovedStudySets(String folderId, Set<String> activeIds) {
        studySetRepository.findAllBySyllabusFolderIdAndDeletedFalse(folderId).stream()
                .filter(ss -> !activeIds.contains(ss.getId()))
                .forEach(ss -> {
                    ss.setDeleted(true);
                    studySetRepository.save(ss);
                    log.info("[Sync] Soft-deleted study set: {}", ss.getName());
                });
    }

    private void softDeleteRemovedSteps(String studySetId, Set<String> activeIds) {
        stepRepository.findAllBySyllabusStudySetIdAndDeletedFalse(studySetId).stream()
                .filter(s -> !activeIds.contains(s.getId()))
                .forEach(s -> {
                    s.setDeleted(true);
                    stepRepository.save(s);
                    log.info("[Sync] Soft-deleted step: {}", s.getName());
                });
    }

    private void softDeleteRemovedActivities(String stepId, Set<String> activeIds) {
        activityRepository.findAllBySyllabusStepIdAndDeletedFalse(stepId).stream()
                .filter(a -> !activeIds.contains(a.getId()))
                .forEach(a -> {
                    a.setDeleted(true);
                    activityRepository.save(a);
                    log.info("[Sync] Soft-deleted activity: {}", a.getName());
                });
    }

    // ==================== Utility ====================

    private String computeHash(String... values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = Arrays.stream(values)
                    .map(v -> v == null ? "" : v)
                    .collect(Collectors.joining("|"));
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }

    private BigDecimal parsePrice(String price) {
        if (price == null || price.isBlank()) return null;
        try {
            return new BigDecimal(price);
        } catch (NumberFormatException e) {
            log.warn("[Sync] Invalid price format: {}", price);
            return null;
        }
    }
}
