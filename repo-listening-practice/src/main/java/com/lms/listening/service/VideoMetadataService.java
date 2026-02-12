package com.lms.listening.service;

import com.lms.listening.dto.request.AddVideoRequest;
import com.lms.listening.dto.response.VideoMetadataResponse;

import java.util.List;

public interface VideoMetadataService {

    /**
     * Add video to study set by fetching metadata from multimedia service
     *
     * @param studySetId Study set ID
     * @param request    Video code and display order
     * @param userId     User ID for authorization
     * @return Created video metadata
     */
    VideoMetadataResponse addVideoToStudySet(String studySetId, AddVideoRequest request, String userId);

    /**
     * Get all videos in a study set
     *
     * @param studySetId Study set ID
     * @return List of video metadata
     */
    List<VideoMetadataResponse> getVideosInStudySet(String studySetId);

    /**
     * Remove video from study set
     *
     * @param studySetId Study set ID
     * @param videoCode  Video code
     * @param userId     User ID for authorization
     */
    void removeVideoFromStudySet(String studySetId, String videoCode, String userId);

    /**
     * Update video display order
     *
     * @param studySetId   Study set ID
     * @param videoCode    Video code
     * @param displayOrder New display order
     * @param userId       User ID for authorization
     * @return Updated video metadata
     */
    VideoMetadataResponse updateVideoDisplayOrder(String studySetId, String videoCode, Integer displayOrder,
            String userId);

    /**
     * Refresh video metadata from multimedia service
     * (In case metadata changed in multimedia service)
     *
     * @param studySetId Study set ID
     * @param videoCode  Video code
     * @return Refreshed video metadata
     */
    VideoMetadataResponse refreshVideoMetadata(String studySetId, String videoCode);
}
