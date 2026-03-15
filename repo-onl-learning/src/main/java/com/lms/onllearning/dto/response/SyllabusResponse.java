package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Maps to CMS API response for syllabus list/detail.
 * Annotated with @JsonIgnoreProperties to be resilient to CMS schema changes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyllabusResponse(
    String id,
    String code,
    String name,
    String hskLevel,
    String type,
    String version,
    String description,
    String distributionHours,
    String trainingProgram,
    String minimumPassingScore,
    String scoreRange,
    String status,
    String prerequisite,
    List<String> teachingMethods,
    List<String> learningTools
) {}
