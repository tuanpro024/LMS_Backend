package com.lms.kanjiorigin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuestionAssignmentResponse {
    private String id;
    private String kanjiLessonId;
    private String kanjiQuestionId;
    private Integer contentIndex;
    private QuestionResponse question;
}
