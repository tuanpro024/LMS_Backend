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

    private String sinoVn;

    private String wordType;

    private String hskLevel;

    @Column(columnDefinition = "TEXT")
    private String examplePinyin;

    @Column(columnDefinition = "TEXT")
    private String exampleMeaning;

    @Column(columnDefinition = "TEXT")
    private String sinoOrigin;

    private String imageWord;

    private String imageOrigin;

    @Column(columnDefinition = "TEXT")
    private String characters;
}
