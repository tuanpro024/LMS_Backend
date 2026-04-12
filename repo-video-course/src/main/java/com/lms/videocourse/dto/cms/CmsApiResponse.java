package com.lms.videocourse.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper for CMS API responses: { statusCode, data, message, success }.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsApiResponse<T> {

    @JsonProperty("statusCode")
    private int statusCode;

    private T data;

    private String message;

    private boolean success;
}
