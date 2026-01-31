package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kanji_question_wrong_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiQuestionWrongOption extends BaseEntity {

    @Column(nullable = false)
    private String wrongOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_question_id", nullable = false)
    private KanjiQuestion kanjiQuestion;
}
