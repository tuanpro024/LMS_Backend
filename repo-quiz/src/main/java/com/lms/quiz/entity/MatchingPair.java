package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Một cặp nối trong MATCHING_PAIRS.
 * Cột A (prompt) - Cột B (answer).
 */
@Entity
@Table(name = "matching_pairs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingPair extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String promptMediaUrl;

    @Column(columnDefinition = "TEXT")
    private String answerMediaUrl;
}
