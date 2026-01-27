package com.lms.content.common.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.content.common.entity.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Video code from Express.js

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 26)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    private Integer duration; // Duration in seconds

    private String thumbnailPath; // MinIO path to thumbnail

    private String karaokePath; // MinIO path to karaoke JSON

    @ManyToMany(mappedBy = "videos")
    @Builder.Default
    private List<StudySet> studySets = new ArrayList<>();
}
