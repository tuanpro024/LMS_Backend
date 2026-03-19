package com.lms.onllearning.dto.response;

public record OnlineCourseFullDetailResponse(
        OnlineCourseResponse course,
        CmsEnvelope<SyllabusDetailResponse> syllabus
) {}