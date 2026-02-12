package com.lms.quiz.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Khối từ cho SENTENCE_BUILDER.
 * correctPosition xác định thứ tự đúng.
 */
@Entity
@Table(name = "sentence_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentenceChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer correctPosition;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDistractor = false;
}
