package com.lms.content.common.delegate.api;

import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.service.StudySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StudySetApiDelegate {

    private final StudySetService studySetService;

    public StudySetResponse createStudySet(CreateStudySetRequest request, String userId) {
        return studySetService.createStudySet(request, userId);
    }

    public StudySetResponse getStudySetById(String id) {
        return studySetService.getStudySetById(id);
    }

    public List<StudySetResponse> getStudySetsByUserId(String userId) {
        return studySetService.getStudySetsByUserId(userId);
    }

    public List<StudySetResponse> getStudySetsByFolderId(String folderId) {
        return studySetService.getStudySetsByFolderId(folderId);
    }

    public List<StudySetResponse> searchStudySets(String keyword) {
        return studySetService.searchStudySets(keyword);
    }

    public List<StudySetResponse> getAllStudySets() {
        return studySetService.getAllStudySets();
    }

    public List<StudySetResponse> findByTitleAndUserIdIgnoreCase(String title, String userId) {
        return studySetService.findByTitleAndUserIdIgnoreCase(title, userId);
    }

    public StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId) {
        return studySetService.updateStudySet(id, request, userId);
    }

    public void deleteStudySet(String id, String userId) {
        studySetService.deleteStudySet(id, userId);
    }
}
