package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.flashcard.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    @Column(nullable = false)
    private String term;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(nullable = false)
    private int cardIndex;

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

    @Column(columnDefinition = "JSON")
    private String characters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id", nullable = false)
    private StudySet studySet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardStatus status = CardStatus.NOT_LEARNED;

}
