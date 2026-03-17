package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Maps list item từ CMS API: GET /api/erp/syllabus
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyllabusResponse(
    String id,
    String code,
    String name,
    @JsonProperty("hsk_level") String hskLevel,
    String type,
    String status,
    String author,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt
) {}
