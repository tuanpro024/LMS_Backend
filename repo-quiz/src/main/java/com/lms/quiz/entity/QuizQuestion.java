package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.quiz.entity.enums.DifficultyLevel;
import com.lms.quiz.entity.enums.FillBlankMode;
import com.lms.quiz.entity.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Câu hỏi trong Quiz - thiết kế đa hình qua QuestionType.
 * Mỗi questionType sẽ dùng các collection con tương ứng:
 * - MULTIPLE_CHOICE → options
 * - FILL_IN_BLANK → blanks
 * - MATCHING_PAIRS → matchingPairs
 * - SENTENCE_BUILDER → sentenceChunks
 */
@Entity
@Table(name = "quiz_questions", indexes = {
        @Index(name = "idx_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_quiz_order", columnList = "quiz_id, question_index")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private Integer questionIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType questionType;

    // ===== Nội dung câu hỏi chung =====
    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String questionMediaUrl;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    // ===== FILL_IN_BLANK specific =====
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private FillBlankMode fillBlankMode;

    @Column(columnDefinition = "TEXT")
    private String sentenceTemplate;

    // ===== SENTENCE_BUILDER specific =====
    @Column(columnDefinition = "TEXT")
    private String correctSentence;

    @Column(columnDefinition = "TEXT")
    private String translationHint;

    // ===== Collections cho từng loại =====
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionIndex ASC")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizBlank> blanks = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchingPair> matchingPairs = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("correctPosition ASC")
    @Builder.Default
    private List<SentenceChunk> sentenceChunks = new ArrayList<>();
}
