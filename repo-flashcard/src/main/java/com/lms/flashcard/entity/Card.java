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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id", nullable = false)
    private StudySet studySet;


}
