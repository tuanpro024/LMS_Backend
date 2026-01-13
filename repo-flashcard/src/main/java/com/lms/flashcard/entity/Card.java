package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
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

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String pinyin;

    @Column(columnDefinition = "TEXT")
    private String pronunciation;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id", nullable = false)
    private StudySet studySet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private com.lms.flashcard.entity.enums.CardStatus status = com.lms.flashcard.entity.enums.CardStatus.NOT_LEARNED;

}
