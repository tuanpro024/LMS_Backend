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
public class KanjiLessonResponse {
    private String id;
    private String title;
    private String description;
    private Integer contentIndex;
    private String studySetId;
    private int kanjiCount;
    private int questionCount;
    private List<KanjiOriginResponse> kanjiOrigins;
    private List<QuestionResponse> questions;
}
