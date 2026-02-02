package com.lms.kanjiorigin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private String id;
    private String kanjiLessonId;
    private String content;
    private String correctAnswer;
    private List<String> wrongOptions;
}
