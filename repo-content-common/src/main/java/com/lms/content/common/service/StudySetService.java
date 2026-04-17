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

    List<StudySetResponse> getStudySetsByPackageType(String packageType);

    List<StudySetResponse> findByTitleAndUserIdIgnoreCase(String title, String userId);

    /**
     * Learning is allowed when study set is linked to at least one active
     * PUBLISHED package, or when it is not linked to any package.
     */
    boolean isStudySetLearningAllowed(String studySetId);

    /**
     * Throws ApiException when study set is not available for learning.
     */
    void assertStudySetLearningAllowed(String studySetId);

    /**
     * Revert all parent packages of a study set to DRAFT when lower-level content
     * changes.
     */
    void revertParentPackagesToDraft(String studySetId, String triggeredBy);

    StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId);

    void deleteStudySet(String id, String userId);
}
