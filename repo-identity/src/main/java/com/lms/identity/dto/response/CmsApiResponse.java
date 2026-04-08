package com.lms.identity.dto.response;

import lombok.Builder;

@Builder
public record CmsApiResponse<T>(
    int statusCode,
    T data,
    String message,
    boolean success
) {}
