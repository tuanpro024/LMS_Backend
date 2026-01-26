package com.lms.content.common.service;

import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;

import java.util.List;

public interface StudySetService {

    StudySetResponse createStudySet(CreateStudySetRequest request, String userId);

    StudySetResponse getStudySetById(String id);

    List<StudySetResponse> getStudySetsByUserId(String userId);

    List<StudySetResponse> getStudySetsByFolderId(String folderId);

    List<StudySetResponse> searchStudySets(String keyword);

    List<StudySetResponse> getAllStudySets();

    StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId);

    void deleteStudySet(String id, String userId);
}
