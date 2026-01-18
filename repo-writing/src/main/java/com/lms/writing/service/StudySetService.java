package com.lms.writing.service;

import com.lms.writing.dto.request.CreateStudySetRequest;
import com.lms.writing.dto.request.UpdateStudySetRequest;
import com.lms.writing.dto.response.StudySetResponse;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.entity.enums.WordStatus;

import java.util.List;

public interface StudySetService {

    StudySetResponse createStudySet(CreateStudySetRequest request, String userId);

    StudySetResponse getStudySetById(String id, String currentUserId);

    List<StudySetResponse> getAllPublicStudySets();

    List<StudySetResponse> getStudySetsByUserId(String userId, String currentUserId);

    List<StudySetResponse> searchStudySets(String query, String currentUserId);

    StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId);

    void deleteStudySet(String id, String userId);

    List<WordResponse> getWordsByStatus(String studySetId, WordStatus status);

    long getCountByStatus(String studySetId, WordStatus status);
}
