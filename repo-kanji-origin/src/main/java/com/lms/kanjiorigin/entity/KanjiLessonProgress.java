package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntityLongId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kanji_lesson_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "kanji_lesson_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiLessonProgress extends BaseEntityLongId {

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_lesson_id", nullable = false)
    private KanjiLesson kanjiLesson;

    @Column(name = "is_learned")
    @Builder.Default
    private boolean isLearned = false;

}
