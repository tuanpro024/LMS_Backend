package com.lms.writing.entity;

import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;

/**
 * Word entity - extends BaseContentItem which contains common fields:
 * - studySet relationship
 */
@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word extends BaseContentItem {

    // Word-specific fields
    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String pinyin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String example;

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
    private String characters;

}
