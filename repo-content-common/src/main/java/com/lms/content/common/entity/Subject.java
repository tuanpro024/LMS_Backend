package com.lms.content.common.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 26)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Slot> slots = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    // Helper methods
    public void addSlot(Slot slot) {
        slots.add(slot);
        slot.setSubject(this);
    }

    public void removeSlot(Slot slot) {
        slots.remove(slot);
        slot.setSubject(null);
    }

    public void addFolder(Folder folder) {
        folders.add(folder);
        folder.setSubject(this);
    }

    public void removeFolder(Folder folder) {
        folders.remove(folder);
        folder.setSubject(null);
    }
}
