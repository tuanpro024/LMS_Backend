package com.lms.videocourse.service;

import com.lms.videocourse.dto.SyllabusTreeDTO;
import com.lms.videocourse.entity.SyllabusFolder;
import com.lms.videocourse.entity.SyllabusPackage;
import com.lms.videocourse.entity.SyllabusStudySet;
import com.lms.videocourse.repository.SyllabusFolderRepository;
import com.lms.videocourse.repository.SyllabusPackageRepository;
import com.lms.videocourse.repository.SyllabusStepRepository;
import com.lms.videocourse.repository.SyllabusStudySetRepository;
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

    /**
     * Fetches the entire Syllabus hierarchy: Packages -> Folders -> StudySets -> Steps.
     * Groups them into a list of SyllabusTreeDTOs.
     */
    public List<SyllabusTreeDTO> getFullSyllabusTree() {
        // Fetch all active records
        List<SyllabusPackage> packages = packageRepository.findAllByDeletedFalse();
        List<SyllabusFolder> folders = folderRepository.findAllByDeletedFalse();
        List<SyllabusStudySet> studySets = studySetRepository.findAllByDeletedFalse();
        List<com.lms.videocourse.entity.SyllabusStep> steps = stepRepository.findAllByDeletedFalse();

        // Group folders by package ID
        Map<String, List<SyllabusFolder>> foldersByPackage = folders.stream()
                .collect(Collectors.groupingBy(SyllabusFolder::getSyllabusPackageId));

        // Group study sets by folder ID
        Map<String, List<SyllabusStudySet>> studySetsByFolder = studySets.stream()
                .collect(Collectors.groupingBy(SyllabusStudySet::getSyllabusFolderId));

        // Group steps by study set ID
        Map<String, List<com.lms.videocourse.entity.SyllabusStep>> stepsByStudySet = steps.stream()
                .collect(Collectors.groupingBy(com.lms.videocourse.entity.SyllabusStep::getSyllabusStudySetId));

        // Assemble the tree
        return packages.stream().map(pkg -> SyllabusTreeDTO.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .category(pkg.getCategory())
                .folders(foldersByPackage.getOrDefault(pkg.getId(), List.of()).stream()
                        .map(folder -> SyllabusTreeDTO.FolderDTO.builder()
                                .id(folder.getId())
                                .name(folder.getName())
                                .description(folder.getDescription())
                                .studySets(studySetsByFolder.getOrDefault(folder.getId(), List.of()).stream()
                                        .map(ss -> SyllabusTreeDTO.StudySetDTO.builder()
                                                .id(ss.getId())
                                                .name(ss.getName())
                                                .description(ss.getDescription())
                                                .steps(stepsByStudySet.getOrDefault(ss.getId(), List.of()).stream()
                                                        .map(step -> SyllabusTreeDTO.StepDTO.builder()
                                                                .id(step.getId())
                                                                .name(step.getName())
                                                                .description(step.getDescription())
                                                                .moduleName(step.getModuleName())
                                                                .build())
                                                        .collect(Collectors.toList()))
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build())
                .collect(Collectors.toList());
    }
}
