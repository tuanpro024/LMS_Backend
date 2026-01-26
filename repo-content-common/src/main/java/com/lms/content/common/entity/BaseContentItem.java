package com.lms.content.common.entity;

import com.lms.common.jpa.BaseEntity;
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

}
