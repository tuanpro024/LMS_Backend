package com.lms.learningpath.service.impl;

import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.learningpath.entity.StudySetProgress;
import com.lms.learningpath.entity.StudySetUnlockRule;
import com.lms.learningpath.repository.StudySetProgressRepository;
import com.lms.learningpath.repository.StudySetUnlockRuleRepository;
import com.lms.learningpath.service.IRealtimeNotificationService;
import com.lms.learningpath.service.IUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnlockServiceImpl implements IUnlockService {

    private final StudySetUnlockRuleRepository unlockRuleRepo;
    private final StudySetProgressRepository progressRepo;
    private final StudySetRepository studySetRepo;
    private final IRealtimeNotificationService realtimeService;

    /**
     * Check if a StudySet is unlocked for user
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isStudySetUnlocked(String userId, String studySetId) {
        // Get unlock rule
        var ruleOpt = unlockRuleRepo.findByStudySetIdAndIsActive(studySetId, true);

        if (ruleOpt.isEmpty()) {
            // No rule = always unlocked
            return true;
        }

        StudySetUnlockRule rule = ruleOpt.get();

        // Check specific required StudySet
        if (rule.getRequiredStudySetId() != null) {
            var requiredProgress = progressRepo.findByUserIdAndStudySetId(userId, rule.getRequiredStudySetId());
            if (requiredProgress.isEmpty() || !requiredProgress.get().canUnlockNext()) {
                return false;
            }
        }

        // Check previous StudySet in same Folder
        if (rule.getRequirePreviousInFolder()) {
            StudySet currentSet = studySetRepo.findById(studySetId).orElse(null);
            if (currentSet == null || currentSet.getFolders().isEmpty()) {
                return true;
            }

            Folder folder = currentSet.getFolders().get(0);
            List<StudySet> allSetsInFolder = folder.getStudySets().stream()
                    .sorted(Comparator.comparing(StudySet::getCreatedAt))
                    .collect(Collectors.toList());

            int currentIndex = -1;
            for (int i = 0; i < allSetsInFolder.size(); i++) {
                if (allSetsInFolder.get(i).getId().equals(studySetId)) {
                    currentIndex = i;
                    break;
                }
            }

            // First set = unlocked
            if (currentIndex == 0) {
                return true;
            }

            // Check previous set
            if (currentIndex > 0) {
                StudySet previousSet = allSetsInFolder.get(currentIndex - 1);
                var previousProgress = progressRepo.findByUserIdAndStudySetId(userId, previousSet.getId());
                return previousProgress.isPresent() && previousProgress.get().canUnlockNext();
            }
        }

        return true;
    }

    /**
     * Get lock reason for display
     */
    @Override
    @Transactional(readOnly = true)
    public String getLockReason(String userId, String studySetId) {
        var ruleOpt = unlockRuleRepo.findByStudySetIdAndIsActive(studySetId, true);

        if (ruleOpt.isEmpty()) {
            return null;
        }

        StudySetUnlockRule rule = ruleOpt.get();

        if (rule.getRequiredStudySetId() != null) {
            StudySet requiredSet = studySetRepo.findById(rule.getRequiredStudySetId()).orElse(null);
            if (requiredSet != null) {
                return "Requires completion of: " + requiredSet.getTitle();
            }
        }

        if (rule.getRequirePreviousInFolder()) {
            return "Complete the previous StudySet first";
        }

        return "Locked";
    }

    /**
     * Check and unlock next StudySet after completion
     */
    @Override
    @Transactional
    public void checkAndUnlockNextStudySet(String userId, StudySet completedSet) {
        if (completedSet.getFolders().isEmpty()) {
            return;
        }

        Folder folder = completedSet.getFolders().get(0);
        List<StudySet> allSets = folder.getStudySets().stream()
                .sorted(Comparator.comparing(StudySet::getCreatedAt))
                .collect(Collectors.toList());

        int currentIndex = -1;
        for (int i = 0; i < allSets.size(); i++) {
            if (allSets.get(i).getId().equals(completedSet.getId())) {
                currentIndex = i;
                break;
            }
        }

        // Notify next set unlocked
        if (currentIndex >= 0 && currentIndex < allSets.size() - 1) {
            StudySet nextSet = allSets.get(currentIndex + 1);
            if (isStudySetUnlocked(userId, nextSet.getId())) {
                realtimeService.notifyStudySetUnlocked(userId, nextSet.getId());
            }
        }
    }
}
