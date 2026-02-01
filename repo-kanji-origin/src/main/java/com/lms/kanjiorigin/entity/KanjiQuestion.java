package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kanji_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiQuestion extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String correctAnswer;

    @OneToMany(mappedBy = "kanjiQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KanjiQuestionWrongOption> wrongOptions = new ArrayList<>();

    @OneToMany(mappedBy = "kanjiQuestion", cascade = CascadeType.ALL)
    @Builder.Default
    private List<KanjiLessonQuestion> lessonQuestions = new ArrayList<>();
}
