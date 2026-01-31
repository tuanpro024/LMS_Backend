package com.lms.kanjiorigin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiLessonBasicResponse {
    private String id;
    private String title;
    private String description;
    private int kanjiCount;
    private int questionCount;
}
