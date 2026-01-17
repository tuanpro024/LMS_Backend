package com.lms.writing.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySet extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false, length = 26)
    private String userId;

    @OneToMany(mappedBy = "studySet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Word> words = new ArrayList<>();

    @ManyToMany(mappedBy = "studySets")
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    // Helper methods
    public void addWord(Word word) {
        words.add(word);
        word.setStudySet(this);
    }

    public void removeWord(Word word) {
        words.remove(word);
        word.setStudySet(null);
    }
}
