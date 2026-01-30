package com.lms.learningpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockedSetInfo {

    private String id;
    private String title;
    private String thumbnail;
    private String message;
}