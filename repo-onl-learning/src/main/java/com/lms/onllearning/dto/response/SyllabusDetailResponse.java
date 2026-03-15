package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Detailed syllabus including schedule (syllabus_schedule rows).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyllabusDetailResponse(
    String id,
    String code,
    String name,
    String hskLevel,
    String type,
    String version,
    String author,
    String description,
    String distributionHours,
    String documentType,
    String trainingProgram,
    String studentResponsibilities,
    String minimumPassingScore,
    String scoreRange,
    String notes,
    String status,
    String prerequisite,
    List<String> teachingMethods,
    List<String> learningTools,
    List<ScheduleItem> schedule
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleItem(
        Integer sessionNo,
        String topic,
        String content,
        String delivery,
        String sessionLo,
        String coreClo,
        String supportingClo,
        String evidence,
        String studentMaterials,
        String teacherMaterials,
        String studentTasks,
        String teacherTasks
    ) {}
}
