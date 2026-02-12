package com.lms.quiz.entity;

import com.lms.content.common.entity.BaseContentItem;
import com.lms.quiz.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz entity - một bài quiz trong StudySet.
 * Extends BaseContentItem chứa: studySet relationship, contentIndex.
 */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends BaseContentItem {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instruction;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeLimitSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer passingScore = 70;

    @Column(nullable = false)
    @Builder.Default
    private Boolean shuffleQuestions = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionIndex ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();
}
