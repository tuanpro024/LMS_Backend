package com.lms.kanjiorigin.entity;

import com.lms.content.common.entity.BaseContentItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kanji_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiLesson extends BaseContentItem {

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
}
