package com.lms.writing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingStudySetProgressResponse {
    private String studySetId;
    private String studySetTitle;
    private String status;
    private Integer learnedWords;
    private Integer totalWords;
    private Double progressPercentage;
}