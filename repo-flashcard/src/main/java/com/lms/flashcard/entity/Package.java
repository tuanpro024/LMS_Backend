package com.lms.flashcard.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 26)
    private String userId;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    // Helper methods
    public void addSubject(Subject subject) {
        subjects.add(subject);
        subject.setPackageEntity(this);
    }

    public void removeSubject(Subject subject) {
        subjects.remove(subject);
        subject.setPackageEntity(null);
    }

    public void addFolder(Folder folder) {
        folders.add(folder);
        folder.setPackageEntity(this);
    }

    public void removeFolder(Folder folder) {
        folders.remove(folder);
        folder.setPackageEntity(null);
    }
}
