package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "kanji_lesson_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiLessonQuestion extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String correctAnswer;

    @ElementCollection
    @CollectionTable(name = "kanji_lesson_question_wrong_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "wrong_option")
    private List<String> wrongOptions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_lesson_id", nullable = false)
    private KanjiLesson kanjiLesson;
}
