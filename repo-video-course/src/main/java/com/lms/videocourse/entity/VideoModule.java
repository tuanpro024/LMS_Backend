package com.lms.videocourse.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a video module (individual video lesson) within a VideoStep.
 * KEY DIFFERENCE from StepModule in repo-learning-path:
 * - VideoModule embeds video content DIRECTLY (videoUrl, thumbnailUrl,
 * duration, subtitles)
 * - No external reference to other modules; video data is stored inline
 * - Optionally linked to repo-multimedia via videoCode for metadata sync
 *
 * The subtitles field stores subtitle data as a JSON string.
 * Example format:
 * [{"startTime":0,"endTime":5,"text":"こんにちは","pinyin":"","translation":"Xin
 * chào"}]
 */
@Entity
@Table(name = "video_modules", indexes = {
        @Index(name = "idx_vm_step", columnList = "step_id"),
        @Index(name = "idx_vm_step_order", columnList = "step_id, module_order"),
        @Index(name = "idx_vm_video_code", columnList = "video_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoModule extends BaseEntity {

    @Column(nullable = false, length = 26)
    private String stepId; // FK to VideoStep

    @Column(nullable = false)
    private Integer moduleOrder; // Display order within step (1, 2, 3, ...)

    @Column(nullable = false, length = 255)
    private String title; // "Bài học 1: Chào hỏi cơ bản"

    @Column(columnDefinition = "TEXT")
    private String description;

    // ===== Inline Video Content (key difference from StepModule) =====

    @Column(columnDefinition = "TEXT")
    private String videoUrl; // HLS playlist URL or direct video URL from repo-multimedia/Node.js

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl; // Thumbnail image URL

    private Integer duration; // Duration in seconds (used for 80% watch-time calculation)

    @Column(columnDefinition = "TEXT")
    private String subtitles; // Subtitle data as JSON string

    // ===== Practice Module Link (for non-video modules) =====

    @Column(length = 50)
    private String moduleType; // FLASHCARD, QUIZ, WRITING, KANJI_ORIGIN, etc.

    @Column(length = 26)
    private String contentSetId; // target study set / content ID in other services

    // ===== Optional link to repo-multimedia =====

    @Column(length = 50)
    private String videoCode; // Optional: Video.code from repo-multimedia (for sync)

    // ===== Module settings =====

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Must be watched to complete the step?

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
