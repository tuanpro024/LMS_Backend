package com.lms.kanjiorigin.dto.request;

import com.lms.common.dto.PaginationRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KanjiQuestionSearchRequest extends PaginationRequest {
    private String keyword;
}
