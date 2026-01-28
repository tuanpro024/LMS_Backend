package com.lms.flashcard.entity;

import com.lms.content.common.dto.excel.ImportableContentItem;
import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;

/**
 * Card entity - extends BaseContentItem which contains common fields:
 * - studySet relationship
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseContentItem implements ImportableContentItem {

    // Card-specific fields
    @Column(nullable = false)
    private int cardIndex;

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

    private String audio;

    @Column(columnDefinition = "TEXT")
    private String characters;
}
