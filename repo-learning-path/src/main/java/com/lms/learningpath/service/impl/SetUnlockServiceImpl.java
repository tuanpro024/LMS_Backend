package com.lms.learningpath.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learningpath.dto.response.UnlockedSetInfo;
import com.lms.learningpath.entity.UserSetProgress;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.entity.enums.SetStatus;
import com.lms.learningpath.repository.UserSetProgressRepository;
import com.lms.learningpath.service.LearningEventService;
import com.lms.learningpath.service.SetUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetUnlockServiceImpl implements SetUnlockService {

    private final UserSetProgressRepository setProgressRepository;
    private final LearningEventService eventService;
    private final ObjectMapper objectMapper;

    // TODO: Inject Feign client to content-common service to get StudySet info

    @Override
    @Transactional(readOnly = true)
    public boolean canUnlockSet(String userId, String studySetId) {
        // TODO: Get StudySet from content-common service
        // String unlockRuleJson = studySet.getUnlockRuleJson();

        // For now, return true (all sets unlocked by default)
        // This will be implemented when we have Feign client to content-common

        log.debug("Checking unlock for set: {} user: {}", studySetId, userId);
        return true;

        /*
        // Future implementation:
        if (unlockRuleJson == null || unlockRuleJson.isEmpty()) {
            return true;  // No rule = always unlocked
        }

        try {
            JsonNode rule = objectMapper.readTree(unlockRuleJson);
            String prerequisiteSetId = rule.get("prerequisiteSetId").asText();
            int minScore = rule.get("minScore").asInt();
            int minRequiredCompleted = rule.has("minRequiredCompleted")
                ? rule.get("minRequiredCompleted").asInt()
                : 0;

            // Check prerequisite set completion
            UserSetProgress prereqProgress = setProgressRepository
                .findByUserIdAndStudySetId(userId, prerequisiteSetId)
                .orElse(null);

            if (prereqProgress == null) {
                return false;  // Prerequisite not started
            }

            if (prereqProgress.getStatus() != SetStatus.COMPLETED) {
                return false;  // Prerequisite not completed
            }

            if (prereqProgress.getBestScore() < minScore) {
                return false;  // Score not high enough
            }

            if (minRequiredCompleted > 0 && prereqProgress.getCompletedModules() < minRequiredCompleted) {
                return false;  // Not enough modules completed
            }

            return true;

        } catch (JsonProcessingException e) {
            log.error("Error parsing unlock rule JSON for set: {}", studySetId, e);
            return false;
        }
        */
    }

    @Override
    @Transactional
    public List<UnlockedSetInfo> unlockNextSets(String userId, String completedSetId) {
        log.info("Unlocking next sets after completing: {}", completedSetId);

        List<UnlockedSetInfo> unlockedSets = new ArrayList<>();

        // TODO: Get all study sets from content-common service
        // Filter sets that have completedSetId as prerequisite
        // Check unlock conditions
        // Create UserSetProgress with UNLOCKED status

        // For now, return empty list
        // This will be implemented when we have Feign client to content-common

        /*
        // Future implementation:
        List<StudySet> allSets = contentCommonClient.getAllStudySets();

        for (StudySet set : allSets) {
            if (set.getUnlockRuleJson() != null) {
                try {
                    JsonNode rule = objectMapper.readTree(set.getUnlockRuleJson());
                    String prerequisiteSetId = rule.get("prerequisiteSetId").asText();

                    if (prerequisiteSetId.equals(completedSetId)) {
                        // This set depends on the completed set
                        if (canUnlockSet(userId, set.getId())) {
                            // Unlock it
                            UserSetProgress progress = setProgressRepository
                                .findByUserIdAndStudySetId(userId, set.getId())
                                .orElseGet(() -> {
                                    UserSetProgress newProgress = UserSetProgress.builder()
                                        .userId(userId)
                                        .studySetId(set.getId())
                                        .status(SetStatus.UNLOCKED)
                                        .completedModules(0)
                                        .totalModules(0)
                                        .earnedExp(0)
                                        .build();
                                    return setProgressRepository.save(newProgress);
                                });

                            if (progress.getStatus() == SetStatus.LOCKED) {
                                progress.setStatus(SetStatus.UNLOCKED);
                                setProgressRepository.save(progress);

                                // Log event
                                eventService.logSimpleEvent(userId, EventType.SET_UNLOCKED, set.getId(), null);

                                unlockedSets.add(UnlockedSetInfo.builder()
                                    .id(set.getId())
                                    .title(set.getTitle())
                                    .thumbnail(set.getThumbnail())
                                    .message("🎊 Chúc mừng! Bạn đã mở khóa " + set.getTitle() + "!")
                                    .build());

                                log.info("Unlocked set: {} for user: {}", set.getId(), userId);
                            }
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing unlock rule for set: {}", set.getId(), e);
                }
            }
        }
        */

        return unlockedSets;
    }

    @Override
    @Transactional
    public void unlockSetForUser(String userId, String studySetId) {
        log.info("Admin unlocking set: {} for user: {}", studySetId, userId);

        UserSetProgress progress = setProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> {
                    // TODO: Get total modules count from module repository or content-common
                    int totalModules = 0;

                    UserSetProgress newProgress = UserSetProgress.builder()
                            .userId(userId)
                            .studySetId(studySetId)
                            .status(SetStatus.UNLOCKED)
                            .completedModules(0)
                            .totalModules(totalModules)
                            .earnedExp(0)
                            .build();
                    return setProgressRepository.save(newProgress);
                });

        if (progress.getStatus() == SetStatus.LOCKED) {
            progress.setStatus(SetStatus.UNLOCKED);
            setProgressRepository.save(progress);

            eventService.logSimpleEvent(userId, EventType.SET_UNLOCKED, studySetId, null);

            log.info("Set unlocked by admin: {} for user: {}", studySetId, userId);
        }
    }
}