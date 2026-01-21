package com.lms.flashcard.entity;

import com.lms.content.common.entity.BaseContentItem;
import com.lms.content.common.entity.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Card entity - extends BaseContentItem which contains common fields:
 * - studySet relationship
 * - contentIndex (replaces cardIndex)
 * - status (CardStatus → ContentStatus)
 * - sinoVn, wordType, hskLevel
 * - examplePinyin, exampleMeaning
 * - sinoOrigin, imageWord, imageOrigin
 * - characters
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseContentItem {

    // Card-specific fields (not in BaseContentItem)
    @Column(nullable = false)
    private String term;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT")
    private String pinyin;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    // Note: The following fields are now inherited from BaseContentItem:
    // - studySet (ManyToOne relationship)
    // - contentIndex (was cardIndex)
    // - status (ContentStatus enum - compatible with CardStatus)
    // - sinoVn, wordType, hskLevel
    // - examplePinyin, exampleMeaning
    // - sinoOrigin, imageWord, imageOrigin
    // - characters
}
