package com.lms.videocourse.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for detail of a CMS package (syllabus) including the full tree:
 * package -> folders[] -> units[] -> video_lessons[] -> modules[]
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsPackageDetailDTO {

    private String id;
    private String name;
    private String description;
    private String category;
    private List<CmsFolder> folders;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CmsFolder {
        private String id;
        private String name;
        private String description;
        @JsonProperty("package_id")
        private String packageId;
        private List<CmsUnit> units;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CmsUnit {
        private String id;
        @JsonProperty("folder_id")
        private String folderId;
        @JsonProperty("session_no")
        private Integer sessionNo;
        private String topic;
        private String content;
        @JsonProperty("video_lessons")
        private List<CmsVideoLesson> videoLessons;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CmsVideoLesson {
        private String id;
        @JsonProperty("unit_id")
        private String unitId;
        private String name;
        private String description;
        @JsonProperty("order_index")
        private Integer orderIndex;
        private List<CmsModule> modules;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CmsModule {
        private String id;
        private String name;
        @JsonProperty("video_lesson_id")
        private String videoLessonId;
        @JsonProperty("video_content")
        private String videoContent;
        @JsonProperty("document_content")
        private String documentContent;
    }
}
