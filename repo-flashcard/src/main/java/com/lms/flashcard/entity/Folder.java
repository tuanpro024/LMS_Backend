package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String color;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false, length = 26)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Folder> subfolders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private Package packageEntity;

    @ManyToMany
    @JoinTable(name = "folder_study_sets", joinColumns = @JoinColumn(name = "folder_id"), inverseJoinColumns = @JoinColumn(name = "study_set_id"))
    @Builder.Default
    private List<StudySet> studySets = new ArrayList<>();

    // Helper methods
    public void addStudySet(StudySet studySet) {
        studySets.add(studySet);
        studySet.getFolders().add(this);
    }

    public void removeStudySet(StudySet studySet) {
        studySets.remove(studySet);
        studySet.getFolders().remove(this);
    }

    public void addSubfolder(Folder subfolder) {
        subfolders.add(subfolder);
        subfolder.setParentFolder(this);
    }

    public void removeSubfolder(Folder subfolder) {
        subfolders.remove(subfolder);
        subfolder.setParentFolder(null);
    }
}
