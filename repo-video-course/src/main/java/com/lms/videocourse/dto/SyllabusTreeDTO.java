package com.lms.videocourse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusTreeDTO {

    private String id;
    private String name;
    private String description;
    private String category;
    private List<FolderDTO> folders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderDTO {
        private String id;
        private String name;
        private String description;
        private List<StudySetDTO> studySets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudySetDTO {
        private String id;
        private String name;
        private String description;
        private List<StepDTO> steps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDTO {
        private String id;
        private String name;
        private String description;
        private String moduleName;
    }
}
