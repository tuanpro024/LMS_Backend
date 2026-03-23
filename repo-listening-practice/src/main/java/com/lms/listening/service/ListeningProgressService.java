package com.lms.listening.service;

import com.lms.listening.dto.request.UpdateListeningProgressRequest;
import com.lms.listening.dto.response.ListeningStudySetProgressResponse;
import com.lms.listening.dto.response.ListeningVideoProgressResponse;

public interface ListeningProgressService {

    ListeningVideoProgressResponse startWatching(String userId, String studySetId, String videoCode);

    ListeningVideoProgressResponse updateWatchProgress(String userId, String studySetId, String videoCode, UpdateListeningProgressRequest request);

    ListeningVideoProgressResponse forceComplete(String userId, String studySetId, String videoCode);

    ListeningVideoProgressResponse getVideoProgress(String userId, String studySetId, String videoCode);

    ListeningStudySetProgressResponse getStudySetProgress(String userId, String studySetId);
}
