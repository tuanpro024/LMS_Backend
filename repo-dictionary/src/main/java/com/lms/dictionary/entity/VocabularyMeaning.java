package com.lms.dictionary.entity;

import com.lms.common.jpa.BaseEntityLongId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabulary_meanings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyMeaning extends BaseEntityLongId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")

    private String meaning;

    @Column(name = "example_sentence_cn", columnDefinition = "TEXT")

    private String exampleSentenceCn;

    @Column(name = "example_sentence_vi", columnDefinition = "TEXT")
    private String exampleSentenceVi;

    @Column(name = "example_sentence_en", columnDefinition = "TEXT")
    private String exampleSentenceEn;

    @Column(name = "example_sentence_pinyin", columnDefinition = "TEXT")
    private String exampleSentencePinyin;
}
