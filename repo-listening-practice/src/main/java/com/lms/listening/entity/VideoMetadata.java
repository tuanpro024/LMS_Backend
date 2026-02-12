package com.lms.listening.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * VideoMetadata entity - stores cached video metadata from repo-multimedia
 * This allows listening-practice service to display video information
 * without making real-time calls to multimedia service
 */
@Entity
@Table(name = "listening_video_metadata", uniqueConstraints = @UniqueConstraint(columnNames = { "study_set_id",
        "video_code" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoMetadata extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String studySetId; // Reference to StudySet from repo-content-common

    @Column(nullable = false, length = 50)
    private String videoCode; // Reference to Video.code from repo-multimedia

    // Cached metadata from multimedia service
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailPath; // MinIO path to thumbnail

    private Integer duration; // Duration in seconds

    @Column(nullable = false)
    private Integer displayOrder; // Order in the study set

    // Streaming information
    private String playlistUrl; // HLS playlist URL pattern (if needed for quick access)
}
