package com.lms.dictionary.dto.request;

import com.lms.common.dto.PaginationRequest;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VocabularySearchRequest extends PaginationRequest {
    private String keySearch;
    private Boolean isSingleVocab;
}

