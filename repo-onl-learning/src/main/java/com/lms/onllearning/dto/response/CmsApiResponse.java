package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper cho response từ CMS API (dangch.tech).
 * Format: {"statusCode":200,"data":...,"message":...,"success":true}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CmsApiResponse<T>(
    Integer statusCode,
    T data,
    String message,
    Boolean success
) {}
