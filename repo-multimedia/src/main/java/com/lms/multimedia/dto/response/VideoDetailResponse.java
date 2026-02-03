package com.lms.multimedia.dto.response;

import com.lms.multimedia.entity.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Extended video response for detailed video information
 * Includes list of available subtitles for the video
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailResponse {

    private String id;
    private String code;
    private String name;
    private String description;
    private VideoStatus status;
    private Integer duration;
    private String thumbnailPath;
    private String karaokePath;
    private String studySetId;
    private Boolean hasSubtitle;

    // List of available subtitles for this video
    private List<SubtitleResponse> subtitles;

    private String createdAt;
    private String updatedAt;
}
