package com.lms.videocourse.service;

import com.lms.videocourse.dto.response.AvailableVideoResponse;

import java.util.List;

/**
 * Service for fetching available videos from repo-multimedia.
 * Used by Admin/Teacher when selecting a video to embed in a VideoModule.
 */
public interface IAvailableVideoService {

    /** Get all available videos, optionally filtered by query */
    List<AvailableVideoResponse> getAllAvailableVideos(String query);

    /** Get a single video by its video code */
    AvailableVideoResponse getVideoByCode(String videoCode);
}
