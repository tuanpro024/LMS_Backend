package com.lms.writing.dto.response;

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
    private String imageUrl;
    private String example;
    private String examplePinyin;
    private String exampleMeaning;
    private List<String> characters;
    private String studySetId;
    private Instant createdAt;
    private Instant updatedAt;
}
