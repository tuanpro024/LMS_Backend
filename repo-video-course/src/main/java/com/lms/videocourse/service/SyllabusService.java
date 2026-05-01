package com.lms.videocourse.service;

import com.lms.videocourse.dto.SyllabusTreeDTO;
import com.lms.videocourse.dto.response.SyllabusCourseListDTO;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusPackageRepository packageRepository;
    private final SyllabusFolderRepository folderRepository;
    private final SyllabusStudySetRepository studySetRepository;
    private final SyllabusStepRepository stepRepository;
    private final SyllabusActivityRepository activityRepository;
    private final CmsVideoCourseRepository cmsCourseRepository;

    /**
     * Fetches the entire Syllabus hierarchy: Packages -> Folders -> StudySets -> Steps -> Activities.
     * Groups them into a list of SyllabusTreeDTOs.
     */
    public List<SyllabusTreeDTO> getFullSyllabusTree() {
        // Fetch all active records
        List<SyllabusPackage> packages = packageRepository.findAllByDeletedFalse();
        List<SyllabusFolder> folders = folderRepository.findAllByDeletedFalse();
        List<SyllabusStudySet> studySets = studySetRepository.findAllByDeletedFalse();
        List<SyllabusStep> steps = stepRepository.findAllByDeletedFalse();
        List<SyllabusActivity> activities = activityRepository.findAllByDeletedFalse();

        // Group folders by package ID
        Map<String, List<SyllabusFolder>> foldersByPackage = folders.stream()
                .collect(Collectors.groupingBy(SyllabusFolder::getSyllabusPackageId));

        // Group study sets by folder ID
        Map<String, List<SyllabusStudySet>> studySetsByFolder = studySets.stream()
                .collect(Collectors.groupingBy(SyllabusStudySet::getSyllabusFolderId));

        // Group steps by study set ID
        Map<String, List<SyllabusStep>> stepsByStudySet = steps.stream()
                .collect(Collectors.groupingBy(SyllabusStep::getSyllabusStudySetId));

        // Group activities by step ID
        Map<String, List<SyllabusActivity>> activitiesByStep = activities.stream()
                .collect(Collectors.groupingBy(SyllabusActivity::getSyllabusStepId));

        // Assemble the tree
        return packages.stream().map(pkg -> SyllabusTreeDTO.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .category(pkg.getCategory())
                .courseName(pkg.getCourseName())
                .courseDescription(pkg.getCourseDescription())
                .coursePrice(pkg.getCoursePrice())
                .courseType(pkg.getCourseType())
                .lastSyncedAt(pkg.getLastSyncedAt())
                .folders(foldersByPackage.getOrDefault(pkg.getId(), List.of()).stream()
                        .map(folder -> SyllabusTreeDTO.FolderDTO.builder()
                                .id(folder.getId())
                                .name(folder.getName())
                                .description(folder.getDescription())
                                .displayOrder(folder.getDisplayOrder())
                                .studySets(studySetsByFolder.getOrDefault(folder.getId(), List.of()).stream()
                                        .map(ss -> SyllabusTreeDTO.StudySetDTO.builder()
                                                .id(ss.getId())
                                                .name(ss.getName())
                                                .description(ss.getDescription())
                                                .sessionNo(ss.getSessionNo())
                                                .steps(stepsByStudySet.getOrDefault(ss.getId(), List.of()).stream()
                                                        .map(step -> SyllabusTreeDTO.StepDTO.builder()
                                                                .id(step.getId())
                                                                .name(step.getName())
                                                                .description(step.getDescription())
                                                                .moduleName(step.getModuleName())
                                                                .displayOrder(step.getDisplayOrder())
                                                                .activities(activitiesByStep.getOrDefault(step.getId(), List.of()).stream()
                                                                        .map(act -> SyllabusTreeDTO.ActivityDTO.builder()
                                                                                .id(act.getId())
                                                                                .cmsModuleId(act.getCmsModuleId())
                                                                                .name(act.getName())
                                                                                .videoContent(act.getVideoContent())
                                                                                .documentContent(act.getDocumentContent())
                                                                                .displayOrder(act.getDisplayOrder())
                                                                                .build())
                                                                        .collect(Collectors.toList()))
                                                                .build())
                                                        .collect(Collectors.toList()))
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build())
                .collect(Collectors.toList());
    }

    /**
     * Get summary list of all synced CMS courses.
     */
    public List<SyllabusCourseListDTO> getCourseList() {
        List<CmsVideoCourse> courses = cmsCourseRepository.findAllByDeletedFalse();

        // Find corresponding syllabus package names
        Map<String, SyllabusPackage> pkgByCourseId = packageRepository
                .findAllByCmsSyllabusIdIsNotNullAndDeletedFalse().stream()
                .collect(Collectors.toMap(
                        p -> p.getCmsCourseId() != null ? p.getCmsCourseId() : "",
                        p -> p,
                        (a, b) -> a
                ));

        return courses.stream().map(c -> {
            SyllabusPackage pkg = pkgByCourseId.get(c.getCmsCourseId());
            return SyllabusCourseListDTO.builder()
                    .id(c.getId())
                    .cmsCourseId(c.getCmsCourseId())
                    .code(c.getCode())
                    .name(c.getName())
                    .courseType(c.getCourseType())
                    .level(c.getLevel())
                    .price(c.getPrice())
                    .totalLessons(c.getTotalLessons())
                    .syllabusId(c.getSyllabusId())
                    .syllabusName(pkg != null ? pkg.getName() : null)
                    .lastSyncedAt(c.getLastSyncedAt())
                    .build();
        }).collect(Collectors.toList());
    }

        public long countActiveCourses() {
                return cmsCourseRepository.countByDeletedFalse();
        }

    /**
     * Get full syllabus tree for a specific CMS course.
     */
    public SyllabusTreeDTO getCourseDetail(String cmsCourseId) {
        // Find the CMS course
        CmsVideoCourse course = cmsCourseRepository.findByCmsCourseId(cmsCourseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + cmsCourseId));

        // Find the package
        SyllabusPackage pkg = packageRepository
                .findByCmsSyllabusIdAndCmsCourseId(course.getSyllabusId(), course.getCmsCourseId())
                .orElse(null);

        if (pkg == null) {
            return SyllabusTreeDTO.builder()
                    .courseName(course.getName())
                    .courseType(course.getCourseType())
                    .coursePrice(course.getPrice())
                    .lastSyncedAt(course.getLastSyncedAt())
                    .folders(List.of())
                    .build();
        }

        // Build tree for this package only
        List<SyllabusFolder> folders = folderRepository.findAllBySyllabusPackageIdAndDeletedFalse(pkg.getId());

        List<String> folderIds = folders.stream().map(SyllabusFolder::getId).collect(Collectors.toList());

        List<SyllabusStudySet> studySets = studySetRepository.findAllByDeletedFalse().stream()
                .filter(ss -> folderIds.contains(ss.getSyllabusFolderId()))
                .collect(Collectors.toList());

        List<String> studySetIds = studySets.stream().map(SyllabusStudySet::getId).collect(Collectors.toList());

        List<SyllabusStep> steps = stepRepository.findAllByDeletedFalse().stream()
                .filter(s -> studySetIds.contains(s.getSyllabusStudySetId()))
                .collect(Collectors.toList());

        List<String> stepIds = steps.stream().map(SyllabusStep::getId).collect(Collectors.toList());

        List<SyllabusActivity> activities = activityRepository.findAllBySyllabusStepIdInAndDeletedFalse(stepIds);

        // Group
        Map<String, List<SyllabusStudySet>> ssByFolder = studySets.stream()
                .collect(Collectors.groupingBy(SyllabusStudySet::getSyllabusFolderId));
        Map<String, List<SyllabusStep>> stepsBySS = steps.stream()
                .collect(Collectors.groupingBy(SyllabusStep::getSyllabusStudySetId));
        Map<String, List<SyllabusActivity>> actsByStep = activities.stream()
                .collect(Collectors.groupingBy(SyllabusActivity::getSyllabusStepId));

        return SyllabusTreeDTO.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .category(pkg.getCategory())
                .courseName(pkg.getCourseName())
                .courseDescription(pkg.getCourseDescription())
                .coursePrice(pkg.getCoursePrice())
                .courseType(pkg.getCourseType())
                .lastSyncedAt(pkg.getLastSyncedAt())
                .folders(folders.stream()
                        .map(f -> SyllabusTreeDTO.FolderDTO.builder()
                                .id(f.getId())
                                .name(f.getName())
                                .description(f.getDescription())
                                .displayOrder(f.getDisplayOrder())
                                .studySets(ssByFolder.getOrDefault(f.getId(), List.of()).stream()
                                        .map(ss -> SyllabusTreeDTO.StudySetDTO.builder()
                                                .id(ss.getId())
                                                .name(ss.getName())
                                                .description(ss.getDescription())
                                                .sessionNo(ss.getSessionNo())
                                                .steps(stepsBySS.getOrDefault(ss.getId(), List.of()).stream()
                                                        .map(step -> SyllabusTreeDTO.StepDTO.builder()
                                                                .id(step.getId())
                                                                .name(step.getName())
                                                                .description(step.getDescription())
                                                                .moduleName(step.getModuleName())
                                                                .displayOrder(step.getDisplayOrder())
                                                                .activities(actsByStep.getOrDefault(step.getId(), List.of()).stream()
                                                                        .map(act -> SyllabusTreeDTO.ActivityDTO.builder()
                                                                                .id(act.getId())
                                                                                .cmsModuleId(act.getCmsModuleId())
                                                                                .name(act.getName())
                                                                                .videoContent(act.getVideoContent())
                                                                                .documentContent(act.getDocumentContent())
                                                                                .displayOrder(act.getDisplayOrder())
                                                                                .build())
                                                                        .collect(Collectors.toList()))
                                                                .build())
                                                        .collect(Collectors.toList()))
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
