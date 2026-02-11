package com.lms.pronunciation.dto.request;

import com.lms.common.dto.PaginationRequest;
import com.lms.pronunciation.entity.enums.PronunciationType;
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
public class PronunciationItemSearchRequest extends PaginationRequest {
    private String studySetId;
    private PronunciationType type;
    private String keyword;
}
