package com.lms.flashcard.service;

import com.lms.flashcard.dto.request.CreateStudySetRequest;
import com.lms.flashcard.dto.request.UpdateStudySetRequest;
import com.lms.flashcard.dto.response.StudySetResponse;

import java.util.List;

public interface StudySetService {

    StudySetResponse createStudySet(CreateStudySetRequest request, String userId);

    StudySetResponse getStudySetById(String id, String currentUserId);

    List<StudySetResponse> getAllPublicStudySets();

    List<StudySetResponse> getStudySetsByUserId(String userId, String currentUserId);

    List<StudySetResponse> searchStudySets(String query, String currentUserId);

    StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId);

    void deleteStudySet(String id, String userId);

    // Statistics - for specific user
    List<com.lms.flashcard.dto.response.CardResponse> getCardsByStatusForUser(
            String studySetId,
            com.lms.flashcard.entity.enums.CardStatus status,
            String userId);

    long getCountByStatusForUser(
            String studySetId, 
            com.lms.flashcard.entity.enums.CardStatus status,
            String userId);

    // Statistics - deprecated (backward compatibility, no user context)
    @Deprecated
    List<com.lms.flashcard.dto.response.CardResponse> getCardsByStatus(
            String studySetId,
            com.lms.flashcard.entity.enums.CardStatus status);

    @Deprecated
    long getCountByStatus(String studySetId, com.lms.flashcard.entity.enums.CardStatus status);
}
