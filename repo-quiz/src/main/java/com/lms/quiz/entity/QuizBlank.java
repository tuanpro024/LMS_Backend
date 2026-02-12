package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Đại diện cho một chỗ trống trong FILL_IN_BLANK.
 * Một câu có thể có nhiều blanks.
 * acceptedAnswers là JSON array: ["went", "had gone"]
 */
@Entity
@Table(name = "quiz_blanks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizBlank extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false)
    private Integer blankIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String acceptedAnswers;

    @Column(columnDefinition = "TEXT")
    private String hint;
}
