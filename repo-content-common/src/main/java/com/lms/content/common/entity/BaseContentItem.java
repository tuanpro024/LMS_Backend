package com.lms.content.common.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.content.common.entity.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for content items (Card, Word, etc.)
 * Contains common fields shared across all content types
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseContentItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_set_id", nullable = false)
    private StudySet studySet;

    @Column(nullable = false)
    private int contentIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.NOT_LEARNED;

    // Common Chinese learning fields
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
