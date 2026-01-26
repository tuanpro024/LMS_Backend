package com.lms.writing.dto.response;

import com.lms.writing.entity.enums.WordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordResponse {
    private String id;
    private String word;
    private String pinyin;
    private String meaning;
    private String sinoVn;
    private String wordType;
    private String hskLevel;
    private String example;
    private String examplePinyin;
    private String exampleMeaning;
    private String sinoOrigin;
    private String imageWord;
    private String imageOrigin;
    private String audio;
    private List<String> characters;
    private WordStatus status;
    private String studySetId;
    private Instant createdAt;
    private Instant updatedAt;
}
