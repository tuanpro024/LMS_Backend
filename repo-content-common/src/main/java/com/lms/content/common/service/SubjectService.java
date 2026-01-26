package com.lms.content.common.service;

import com.lms.content.common.dto.request.CreateSubjectRequest;
import com.lms.content.common.dto.request.UpdateSubjectRequest;
import com.lms.content.common.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(CreateSubjectRequest request, String userId);

    SubjectResponse getSubjectById(String id);

    List<SubjectResponse> getSubjectsByPackageId(String packageId);

    List<SubjectResponse> getAllSubjects();

    SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId);

    void deleteSubject(String id, String userId);

    SubjectResponse addFolderToSubject(String subjectId, String folderId, String userId);

    SubjectResponse removeFolderFromSubject(String subjectId, String folderId, String userId);
}
