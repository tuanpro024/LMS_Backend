package com.lms.kanjiorigin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportQuestionRequest {
    private String content;
    private String correctAnswer;
    private List<String> wrongOptions;
}
