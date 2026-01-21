package com.lms.content.common.entity;

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

    // Note: Content items (Cards/Words) relationship handled by BaseContentItem
    // Each specific implementation will have @OneToMany for their content type

    @ManyToMany(mappedBy = "studySets")
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();
}
