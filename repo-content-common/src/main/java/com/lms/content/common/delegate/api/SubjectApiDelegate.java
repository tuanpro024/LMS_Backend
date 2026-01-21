package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreateSubjectRequest;
import com.lms.content.common.dto.request.UpdateSubjectRequest;
import com.lms.content.common.dto.response.SubjectResponse;
import com.lms.content.common.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * API Delegate for Subject operations
 * Returns pure DTOs - NO ResponseEntity
 */
@Component
@RequiredArgsConstructor
public class SubjectApiDelegate {

    private final SubjectService subjectService;

    public SubjectResponse createSubject(CreateSubjectRequest request, String userId) {
        return subjectService.createSubject(request, userId);
    }

    public SubjectResponse getSubjectById(String id) {
        return subjectService.getSubjectById(id);
    }

    public List<SubjectResponse> getSubjectsByPackageId(String packageId) {
        return subjectService.getSubjectsByPackageId(packageId);
    }

    public List<SubjectResponse> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    public SubjectResponse updateSubject(String id, UpdateSubjectRequest request, String userId) {
        return subjectService.updateSubject(id, request, userId);
    }

    public void deleteSubject(String id, String userId) {
        subjectService.deleteSubject(id, userId);
    }

    public SubjectResponse addFolderToSubject(String subjectId, String folderId, String userId) {
        return subjectService.addFolderToSubject(subjectId, folderId, userId);
    }

    public SubjectResponse removeFolderFromSubject(String subjectId, String folderId, String userId) {
        return subjectService.removeFolderFromSubject(subjectId, folderId, userId);
    }
}
