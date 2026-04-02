package com.lms.kanjiorigin.dto.response;

import com.lms.kanjiorigin.entity.enums.KanjiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiStatusResponse {

    private String kanjiId;
    private String studySetId;
    private Integer contentIndex;
    private String term;
    private String pinyin;
    private String meaning;
    private KanjiStatus status;
    private Integer reviewCount;
    private Instant lastReviewedAt;
}