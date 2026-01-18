package com.lms.flashcard.service;

import com.lms.flashcard.dto.response.SubjectResponse;

public interface SubjectService {

    com.lms.flashcard.dto.response.SubjectResponse createSubject(
            com.lms.flashcard.dto.request.CreateSubjectRequest request, String userId);

    SubjectResponse getSubjectById(String id, String userId);

    java.util.List<SubjectResponse> getSubjectsByPackageId(String packageId, String userId);

    java.util.List<SubjectResponse> getAllSubjects(String userId);

    SubjectResponse updateSubject(String id, com.lms.flashcard.dto.request.UpdateSubjectRequest request, String userId);

    void deleteSubject(String id, String userId);

    SubjectResponse addFolderToSubject(String subjectId, String folderId, String userId);

    SubjectResponse removeFolderFromSubject(String subjectId, String folderId, String userId);
}
