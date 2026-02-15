package com.lms.kanjiorigin.entity;

import com.lms.content.common.dto.excel.ImportableContentItem;
import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import com.lms.kanjiorigin.dto.excel.KanjiExtraRowData;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kanji_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiLesson extends BaseContentItem implements ImportableContentItem {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "kanjiLesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "deleted = false")
    @Builder.Default
    private List<KanjiOrigin> kanjiOrigins = new ArrayList<>();

    @OneToMany(mappedBy = "kanjiLesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "deleted = false")
    @Builder.Default
    private List<KanjiLessonQuestion> questions = new ArrayList<>();

    @Transient
    private KanjiOrigin tempImportOrigin;

    @Transient
    private KanjiExtraRowData tempExtraData;
}
