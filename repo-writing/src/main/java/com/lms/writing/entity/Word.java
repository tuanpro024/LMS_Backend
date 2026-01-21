package com.lms.writing.entity;

import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;

/**
 * Word entity - extends BaseContentItem which contains common fields:
 * - studySet relationship
 * - contentIndex (replaces wordIndex)
 * - status (WordStatus → ContentStatus)
 * - sinoVn, wordType, hskLevel
 * - examplePinyin, exampleMeaning
 * - sinoOrigin, imageWord, imageOrigin
 * - characters
 */
@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word extends BaseContentItem {

    // Word-specific fields (not in BaseContentItem)
    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String pinyin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String example;

    // Note: The following fields are now inherited from BaseContentItem:
    // - studySet (ManyToOne relationship)
    // - contentIndex (was wordIndex)
    // - status (ContentStatus enum - compatible with WordStatus)
    // - sinoVn, wordType, hskLevel
    // - examplePinyin, exampleMeaning
    // - sinoOrigin, imageWord, imageOrigin
    // - characters
}
