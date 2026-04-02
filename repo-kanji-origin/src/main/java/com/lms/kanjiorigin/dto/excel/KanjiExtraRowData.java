package com.lms.kanjiorigin.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extra data from columns beyond HierarchicalImportRow (AA onwards)
 * for kanji-origin specific fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiExtraRowData {
    private String originTextCn;
    private String originTextEn;
    private String strokeAnimationUrl;
    private String exampleMeaningVi;
    private String exampleMeaningEn;
}
