package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kanji_lesson_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiLessonQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_lesson_id", nullable = false)
    private KanjiLesson kanjiLesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_question_id", nullable = false)
    private KanjiQuestion kanjiQuestion;

    @Column(nullable = false)
    private Integer contentIndex;
}
