package com.lms.kanjiorigin.dto.response;

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
public class ImportResultResponse {
    private int totalSheets;
    private int lessonsCreated;
    private int lessonsUpdated;
    private int kanjisCreated;
    private int questionsCreated;
    private int questionsReused;
    private boolean success;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}

