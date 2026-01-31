package com.lms.dictionary.entity;

import com.lms.common.jpa.BaseEntityLongId;
import com.lms.dictionary.converter.WordTypeListConverter;
import com.lms.dictionary.enums.WordType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary extends BaseEntityLongId {

    @Column(name = "hsk_level")
    private Integer hskLevel;

    @Convert(converter = WordTypeListConverter.class)
    @Column(name = "word_types", length = 500)
    @Builder.Default
    private List<WordType> wordTypes = new ArrayList<>();

    @Column(nullable = false, length = 50)
    private String hanzi;

    @Column(nullable = false, length = 100)
    private String pinyin;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "stroke_animation_url", length = 500)
    private String strokeAnimationUrl;

    @Column(name = "etymology_story", columnDefinition = "TEXT")

    private String etymologyStory;

    @Column(name = "etymology_image", length = 500)
    private String etymologyImage;

    @Column(name = "is_single_vocab", nullable = false)
    @Builder.Default
    private Boolean isSingleVocab = false;

    @OneToMany(mappedBy = "parentVocab", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<VocabComponent> subVocabs = new ArrayList<>();

    @OneToMany(mappedBy = "componentVocab")
    @Builder.Default
    private List<VocabComponent> parentVocabs = new ArrayList<>();

    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VocabularyMeaning> meanings = new ArrayList<>();
}
