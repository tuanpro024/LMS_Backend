package com.lms.writing.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.writing.entity.enums.WordStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word extends BaseEntity {

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String pinyin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    private int wordIndex;

    private String sinoVn;

    private String wordType;

    private String hskLevel;

    @Column(columnDefinition = "TEXT")
    private String examplePinyin;

    @Column(columnDefinition = "TEXT")
    private String exampleMeaning;

    @Column(columnDefinition = "TEXT")
    private String sinoOrigin;

    private String imageWord;

    private String imageOrigin;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(nullable = false, columnDefinition = "JSON")
    private String characters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id", nullable = false)
    private StudySet studySet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WordStatus status = WordStatus.NOT_LEARNED;
}
