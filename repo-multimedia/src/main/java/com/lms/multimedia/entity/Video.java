package com.lms.multimedia.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.multimedia.entity.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    private Integer duration; // Duration in seconds

    private String thumbnailPath; // MinIO path to thumbnail

    private String karaokePath; // MinIO path to karaoke JSON

    // Unidirectional relationship: Video belongs to one StudySet
    @Column(length = 26)
    private String studySetId; // Nullable - can be set later

    // Track if video has any subtitles
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasSubtitle = false;
}
