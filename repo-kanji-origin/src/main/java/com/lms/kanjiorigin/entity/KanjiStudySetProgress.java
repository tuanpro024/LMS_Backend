package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.kanjiorigin.entity.enums.StudySetProgressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "kanji_study_set_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "study_set_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiStudySetProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "study_set_id", nullable = false, length = 26)
    private String studySetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudySetProgressStatus status;

    @Column(name = "learned_lessons", nullable = false)
    @Builder.Default
    private Integer learnedLessons = 0;

    @Column(name = "total_lessons", nullable = false)
    @Builder.Default
    private Integer totalLessons = 0;

    @Column(name = "progress_percentage", nullable = false)
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Column(name = "first_started_at")
    private Instant firstStartedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
