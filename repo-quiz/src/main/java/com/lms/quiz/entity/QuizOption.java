package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Đáp án cho MULTIPLE_CHOICE.
 * Cũng dùng làm word bank cho FILL_IN_BLANK dạng SELECT.
 */
@Entity
@Table(name = "quiz_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false)
    private Integer optionIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;
}
