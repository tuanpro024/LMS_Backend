package com.lms.flashcard.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.flashcard.dto.request.CreateCardRequest;
import com.lms.flashcard.dto.request.CreateStudySetRequest;
import com.lms.flashcard.dto.request.UpdateStudySetRequest;
import com.lms.flashcard.dto.response.StudySetResponse;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.StudySet;
import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.mapper.CardMapper;
import com.lms.flashcard.mapper.StudySetMapper;
import com.lms.flashcard.repository.StudySetRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import com.lms.flashcard.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudySetServiceImpl implements StudySetService {

    private final StudySetRepository studySetRepository;
    private final StudySetMapper studySetMapper;
    private final CardMapper cardMapper;
    private final com.lms.flashcard.repository.CardRepository cardRepository;
    private final UserCardProgressRepository userCardProgressRepository;

    @Override
    public StudySetResponse createStudySet(CreateStudySetRequest request, String userId) {
        log.info("Creating study set for user: {}", userId);

        StudySet studySet = studySetMapper.toEntity(request);
        studySet.setUserId(userId);

        // Add cards if provided
        if (request.getCards() != null && !request.getCards().isEmpty()) {
            for (CreateCardRequest cardRequest : request.getCards()) {
                Card card = cardMapper.toEntity(cardRequest);
                studySet.addCard(card);
            }
        }

        StudySet saved = studySetRepository.save(studySet);
        return studySetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySetResponse getStudySetById(String id, String currentUserId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check access permission
        if (studySet.isPrivate() && !studySet.getUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.E240, "No permission to view this study set");
        }

        StudySetResponse response = studySetMapper.toResponse(studySet);
        // Calculate progress based on current user's progress
        calculateAndSetProgress(response, studySet, currentUserId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllPublicStudySets() {
        List<StudySet> studySets = studySetRepository.findByIsPrivateFalse();
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByUserId(String userId, String currentUserId) {
        List<StudySet> studySets;

        if (userId.equals(currentUserId)) {
            // User can see all their own study sets
            studySets = studySetRepository.findByUserId(userId);
        } else {
            // Others can only see public study sets
            studySets = studySetRepository.findByUserId(userId).stream()
                    .filter(s -> !s.isPrivate())
                    .toList();
        }

        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> searchStudySets(String query, String currentUserId) {
        List<StudySet> studySets;

        if (currentUserId != null) {
            // Include user's private sets in search
            List<StudySet> publicSets = studySetRepository
                    .findByTitleContainingIgnoreCaseAndIsPrivateFalse(query);
            List<StudySet> userSets = studySetRepository
                    .findByUserIdAndTitleContainingIgnoreCase(currentUserId, query);

            studySets = Stream.concat(publicSets.stream(), userSets.stream())
                    .distinct()
                    .toList();
        } else {
            // Only public sets for anonymous users
            studySets = studySetRepository
                    .findByTitleContainingIgnoreCaseAndIsPrivateFalse(query);
        }

        return studySetMapper.toResponseList(studySets);
    }

    @Override
    public StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check ownership
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this study set");
        }

        // Update fields
        if (request.getTitle() != null) {
            studySet.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            studySet.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            studySet.setPrivate(request.getIsPrivate());
        }

        // Update cards if provided
        if (request.getCards() != null) {
            studySet.getCards().clear();
            for (CreateCardRequest cardRequest : request.getCards()) {
                Card card = cardMapper.toEntity(cardRequest);
                studySet.addCard(card);
            }
        }

        StudySet updated = studySetRepository.save(studySet);
        return studySetMapper.toResponse(updated);
    }

    @Override
    public void deleteStudySet(String id, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check ownership
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this study set");
        }

        studySetRepository.delete(studySet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.lms.flashcard.dto.response.CardResponse> getCardsByStatusForUser(
            String studySetId,
            com.lms.flashcard.entity.enums.CardStatus status,
            String userId) {
        
        if (userId == null) {
            // No user context, return empty list
            return List.of();
        }
        
        List<com.lms.flashcard.entity.UserCardProgress> progressList = 
                userCardProgressRepository.findByUserIdAndStudySetIdAndStatus(userId, studySetId, status);
        
        return progressList.stream()
                .map(ucp -> cardMapper.toResponse(ucp.getCard()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountByStatusForUser(
            String studySetId,
            com.lms.flashcard.entity.enums.CardStatus status,
            String userId) {
        
        if (userId == null) {
            return 0;
        }
        
        return userCardProgressRepository.countByUserIdAndStudySetIdAndStatus(userId, studySetId, status);
    }

    // Deprecated methods for backward compatibility
    @Override
    @Transactional(readOnly = true)
    @Deprecated
    public List<com.lms.flashcard.dto.response.CardResponse> getCardsByStatus(String studySetId,
            com.lms.flashcard.entity.enums.CardStatus status) {
        List<Card> cards = cardRepository.findByStudySetIdAndStatus(studySetId, status);
        return cardMapper.toResponseList(cards);
    }

    @Override
    @Transactional(readOnly = true)
    @Deprecated
    public long getCountByStatus(String studySetId, com.lms.flashcard.entity.enums.CardStatus status) {
        return cardRepository.countByStudySetIdAndStatus(studySetId, status);
    }

    private void calculateAndSetProgress(StudySetResponse response, StudySet studySet, String userId) {
        if (studySet.getCards() == null || studySet.getCards().isEmpty()) {
            response.setProgress(0);
            return;
        }
        
        if (userId == null) {
            // If no user logged in, show 0 progress
            response.setProgress(0);
            return;
        }
        
        long total = studySet.getCards().size();
        // Count learned cards for this specific user
        long learned = userCardProgressRepository.countByUserIdAndStudySetIdAndStatus(
                userId, 
                studySet.getId(), 
                com.lms.flashcard.entity.enums.CardStatus.LEARNED
        );
        
        double progress = ((double) learned / total) * 100;
        // Round to 1 decimal place if needed, or leave as double
        response.setProgress(progress);
    }
}
