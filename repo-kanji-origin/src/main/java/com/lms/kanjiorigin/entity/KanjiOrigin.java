package com.lms.kanjiorigin.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kanji_origins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiOrigin extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanji_lesson_id", nullable = false)
    private KanjiLesson kanjiLesson;

    @Column(nullable = false)
    private Integer contentIndex;

    @Column(nullable = false)
    private String term;

    @Column(columnDefinition = "TEXT")
    private String pinyin;

    private String sinoVn;

    private String meaning;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "stroke_animation_url", length = 500)
    private String strokeAnimationUrl;

    @Column(columnDefinition = "TEXT")
    private String originImage;

    @Column(columnDefinition = "TEXT")
    private String originTextVi;

    @Column(columnDefinition = "TEXT")
    private String originTextCn;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(columnDefinition = "TEXT")
    private String exampleMeaning;

    @Column(columnDefinition = "TEXT")
    private String examplePinyin;

}
