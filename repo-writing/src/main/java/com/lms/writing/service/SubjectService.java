package com.lms.writing.service;

import com.lms.writing.dto.request.CreateSubjectRequest;
import com.lms.writing.dto.request.UpdateSubjectRequest;
import com.lms.writing.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(CreateSubjectRequest request, String userId);

    SubjectResponse getSubjectById(String id);

    List<SubjectResponse> getAllSubjects();

    List<SubjectResponse> getSubjectsByPackageId(String packageId);

    SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId);

    void deleteSubject(String id, String userId);

    SubjectResponse addFolderToSubject(String subjectId, String folderId, String userId);

    SubjectResponse removeFolderFromSubject(String subjectId, String folderId, String userId);
}
