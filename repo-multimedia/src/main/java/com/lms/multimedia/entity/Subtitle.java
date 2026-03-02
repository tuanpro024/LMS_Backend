package com.lms.multimedia.entity;

import com.lms.common.jpa.BaseEntity;
import com.lms.multimedia.entity.enums.SubtitleStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subtitles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subtitle extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String videoId;

    @Column(nullable = false, length = 50)
    private String videoCode; // For MinIO path construction

    @Column(nullable = false)
    private String name; // "Phụ đề chính", "Phụ đề v2", etc.

    @Column(nullable = false, length = 500)
    private String filePath; // MinIO path: /media/{videoCode}/subtitles/{id}.json

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubtitleStatus status = SubtitleStatus.INACTIVE;
}
