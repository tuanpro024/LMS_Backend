package com.lms.writing.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String slotNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 26)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    // Helper methods
    public void addFolder(Folder folder) {
        folders.add(folder);
        folder.setSlot(this);
    }

    public void removeFolder(Folder folder) {
        folders.remove(folder);
        folder.setSlot(null);
    }
}
