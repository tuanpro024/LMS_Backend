package com.lms.learningpath.mapper;

import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.learningpath.dto.external.StudySetDto;

/**
 * Mapper utility to convert between StudySetResponse and StudySetDto.
 * Handles field name differences and type conversions.
 */
public class StudySetMapper {

    private StudySetMapper() {
        // Utility class, prevent instantiation
    }

    /**
     * Convert StudySetResponse (from content services) to StudySetDto
     * (used internally in learning path service).
     *
     * @param response the StudySetResponse from external service
     * @return converted StudySetDto
     */
    public static StudySetDto toDto(StudySetResponse response) {
        if (response == null) {
            return null;
        }

        return StudySetDto.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .thumbnail(response.getThumbnail())
                .isPrivate(response.isPrivate()) // boolean to Boolean (auto-boxing)
                .userId(response.getUserId())
                .estimatedMinutes(response.getEstimatedMinutes())
                .itemCount((long) response.getTotalItems()) // int to Long
                .build();
    }
}
