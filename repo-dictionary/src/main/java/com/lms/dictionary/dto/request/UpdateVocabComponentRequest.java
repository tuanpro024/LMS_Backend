package com.lms.dictionary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVocabComponentRequest {
    
    private Long id;
    private CreateVocabularyRequest newVocabulary;
    private Integer orderIndex;
}
