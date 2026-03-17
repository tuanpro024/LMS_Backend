package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps detail response từ CMS API: GET /api/erp/syllabus/{id}
 * Bao gồm đầy đủ clo, materials, schedule, gradingStructure.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyllabusDetailResponse(
    String id,
    String code,
    String name,
    @JsonProperty("hsk_level")      String hskLevel,
    String type,
    String version,
    String author,
    String description,
    @JsonProperty("distribution_hours")       String distributionHours,
    @JsonProperty("document_type")            String documentType,
    String prerequisite,
    @JsonProperty("training_program")         String trainingProgram,
    @JsonProperty("student_responsibilities") String studentResponsibilities,
    @JsonProperty("minimum_passing_score")    String minimumPassingScore,
    @JsonProperty("score_range")              String scoreRange,
    String notes,
    String status,
    @JsonProperty("teaching_methods")  List<String> teachingMethods,
    @JsonProperty("learning_tools")    List<String> learningTools,
    @JsonProperty("total_sessions")    Integer totalSessions,
    List<CloItem> clo,
    List<MaterialItem> materials,
    List<ScheduleItem> schedule,
    @JsonProperty("gradingStructure") List<GradingItem> gradingStructure
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CloItem(
        String id,
        @JsonProperty("syllabus_id") String syllabusId,
        String name,
        String description
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MaterialItem(
        Long id,
        @JsonProperty("syllabus_id") String syllabusId,
        String title,
        String type,
        String isbn,
        String author,
        String publisher,
        String year,
        String edition
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleItem(
        String id,
        @JsonProperty("syllabus_id")            String syllabusId,
        @JsonProperty("session_no")             Integer sessionNo,
        String topic,
        String content,
        String delivery,
        @JsonProperty("session_lo")             String sessionLo,
        @JsonProperty("core_clo")               String coreClo,
        @JsonProperty("supporting_clo")         String supportingClo,
        String evidence,
        String itu,
        @JsonProperty("student_materials")      String studentMaterials,
        @JsonProperty("teacher_materials")      String teacherMaterials,
        @JsonProperty("student_tasks")          String studentTasks,
        @JsonProperty("teacher_tasks")          String teacherTasks,
        @JsonProperty("student_materials_link") String studentMaterialsLink,
        @JsonProperty("teacher_materials_link") String teacherMaterialsLink,
        @JsonProperty("module_on_luyen")        String moduleOnLuyen
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GradingItem(
        String id,
        @JsonProperty("syllabus_id")        String syllabusId,
        String item,
        String type,
        Integer weight,
        String timing,
        String duration,
        String clo,
        @JsonProperty("organizational_form") String organizationalForm,
        String criteria,
        @JsonProperty("content_scope")       String contentScope
    ) {}
}
