package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HanziWritingResponse {

    private String studySetId;
    private String studySetTitle;
    private List<HanziTermResponse> terms;
}
