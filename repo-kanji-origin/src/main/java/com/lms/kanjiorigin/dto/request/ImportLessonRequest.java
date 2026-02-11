package com.lms.kanjiorigin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportLessonRequest {
    private String title;
    private String description;
    
    @Builder.Default
    private List<ImportKanjiRequest> kanjis = new ArrayList<>();
    
    @Builder.Default
    private List<ImportQuestionRequest> questions = new ArrayList<>();
}
