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

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String unlockRuleJson;

    @Column
    private Integer estimatedMinutes;

    // Note: Content items (Cards/Words) relationship handled by BaseContentItem
    // Each specific implementation will have @OneToMany for their content type

    @ManyToMany(mappedBy = "studySets")
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "study_set_videos", joinColumns = @JoinColumn(name = "study_set_id"), inverseJoinColumns = @JoinColumn(name = "video_id"))
    @Builder.Default
    private List<Video> videos = new ArrayList<>();

    // Helper methods for video management
    public void addVideo(Video video) {
        videos.add(video);
        video.getStudySets().add(this);
    }

    public void removeVideo(Video video) {
        videos.remove(video);
        video.getStudySets().remove(this);
    }
}
