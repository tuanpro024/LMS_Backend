package com.lms.learningpath.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.learningpath.dto.request.CreateQuestRequest;
import com.lms.learningpath.dto.response.QuestProgressInfo;
import com.lms.learningpath.dto.response.QuestResponse;
import com.lms.learningpath.entity.QuestDefinition;
import com.lms.learningpath.entity.UserQuestProgress;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.entity.enums.QuestStatus;
import com.lms.learningpath.entity.enums.QuestType;
import com.lms.learningpath.mapper.QuestMapper;
import com.lms.learningpath.repository.QuestDefinitionRepository;
import com.lms.learningpath.repository.UserQuestProgressRepository;
import com.lms.learningpath.service.LearningEventService;
import com.lms.learningpath.service.QuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestServiceImpl implements QuestService {

    private final QuestDefinitionRepository questDefRepository;
    private final UserQuestProgressRepository questProgressRepository;
    private final QuestMapper questMapper;
    private final LearningEventService eventService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public QuestResponse createQuest(CreateQuestRequest request) {
        log.info("Creating quest: {}", request.getTitle());

        QuestDefinition quest = questMapper.toEntity(request);
        QuestDefinition saved = questDefRepository.save(quest);

        return questMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestResponse> getActiveQuests(String userId) {
        Instant now = Instant.now();
        List<QuestDefinition> activeQuests = questDefRepository.findActiveQuestsAtTime(now);

        return activeQuests.stream()
                .map(quest -> {
                    String period = getPeriodString(quest.getType());
                    UserQuestProgress progress = questProgressRepository
                            .findByUserIdAndQuestIdAndPeriod(userId, quest.getId(), period)
                            .orElseGet(() -> createQuestProgress(userId, quest, period));

                    return questMapper.toResponseWithProgress(quest, progress);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<QuestProgressInfo> updateQuestProgress(String userId, String moduleId, String studySetId) {
        List<QuestProgressInfo> updated = new ArrayList<>();

        Instant now = Instant.now();
        List<QuestDefinition> activeQuests = questDefRepository.findActiveQuestsAtTime(now);

        for (QuestDefinition quest : activeQuests) {
            try {
                String period = getPeriodString(quest.getType());
                UserQuestProgress progress = questProgressRepository
                        .findByUserIdAndQuestIdAndPeriod(userId, quest.getId(), period)
                        .orElseGet(() -> createQuestProgress(userId, quest, period));

                if (progress.getStatus() == QuestStatus.ACTIVE) {
                    boolean progressUpdated = updateQuestProgressValue(progress, quest, moduleId, studySetId);

                    if (progressUpdated) {
                        // Check if completed
                        if (progress.getCurrentValue() >= progress.getTargetValue()) {
                            progress.setStatus(QuestStatus.COMPLETED);
                            progress.setCompletedAt(Instant.now());

                            log.info("Quest completed: {} for user: {}", quest.getTitle(), userId);

                            // Log event
                            eventService.logEvent(
                                    userId,
                                    EventType.QUEST_COMPLETED,
                                    studySetId,
                                    moduleId,
                                    null,
                                    null,
                                    null
                            );
                        }

                        questProgressRepository.save(progress);

                        updated.add(QuestProgressInfo.builder()
                                .questId(quest.getId())
                                .title(quest.getTitle())
                                .status(progress.getStatus().name())
                                .currentValue(progress.getCurrentValue())
                                .targetValue(progress.getTargetValue())
                                .rewardExp(quest.getRewardExp())
                                .build());
                    }
                }
            } catch (Exception e) {
                log.error("Error updating quest progress for quest: {}", quest.getId(), e);
            }
        }

        return updated;
    }

    @Override
    @Transactional
    public QuestResponse claimQuest(String userId, String questId, String period) {
        log.info("User {} claiming quest: {} for period: {}", userId, questId, period);

        QuestDefinition quest = questDefRepository.findById(questId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Quest not found"));

        UserQuestProgress progress = questProgressRepository
                .findByUserIdAndQuestIdAndPeriod(userId, questId, period)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Quest progress not found"));

        if (progress.getStatus() != QuestStatus.COMPLETED) {
            throw new ApiException(ErrorCode.E240, "Quest not completed yet");
        }

        if (progress.getStatus() == QuestStatus.CLAIMED) {
            throw new ApiException(ErrorCode.E240, "Quest already claimed");
        }

        // Claim quest
        progress.setStatus(QuestStatus.CLAIMED);
        progress.setClaimedAt(Instant.now());
        questProgressRepository.save(progress);

        // Log event
        eventService.logEvent(
                userId,
                EventType.QUEST_CLAIMED,
                null,
                null,
                null,
                null,
                "{\"questId\":\"" + questId + "\",\"exp\":" + quest.getRewardExp() + "}"
        );

        // TODO: Award exp to user (call to user service or handle in response)

        log.info("Quest claimed: {} exp: {}", questId, quest.getRewardExp());

        return questMapper.toResponseWithProgress(quest, progress);
    }

    private UserQuestProgress createQuestProgress(String userId, QuestDefinition quest, String period) {
        try {
            JsonNode condition = objectMapper.readTree(quest.getConditionJson());
            int targetValue = condition.has("count") ? condition.get("count").asInt() : 1;

            Instant expiresAt = calculateExpiresAt(quest.getType());

            UserQuestProgress progress = UserQuestProgress.builder()
                    .userId(userId)
                    .quest(quest)
                    .status(QuestStatus.ACTIVE)
                    .currentValue(0)
                    .targetValue(targetValue)
                    .period(period)
                    .expiresAt(expiresAt)
                    .build();

            return questProgressRepository.save(progress);

        } catch (JsonProcessingException e) {
            log.error("Error parsing quest condition JSON", e);
            throw new RuntimeException("Invalid quest condition JSON");
        }
    }

    private boolean updateQuestProgressValue(
            UserQuestProgress progress,
            QuestDefinition quest,
            String moduleId,
            String studySetId
    ) {
        try {
            JsonNode condition = objectMapper.readTree(quest.getConditionJson());
            String conditionType = condition.get("type").asText();

            switch (conditionType) {
                case "COMPLETE_MODULES":
                    // Increment for any module completion
                    progress.setCurrentValue(progress.getCurrentValue() + 1);
                    return true;

                case "COMPLETE_STUDY_SET":
                    // Check if specific study set
                    String requiredSetId = condition.get("studySetId").asText();
                    if (studySetId != null && studySetId.equals(requiredSetId)) {
                        progress.setCurrentValue(1);
                        return true;
                    }
                    return false;

                // Add more condition types as needed

                default:
                    log.warn("Unknown quest condition type: {}", conditionType);
                    return false;
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing quest condition JSON", e);
            return false;
        }
    }

    private String getPeriodString(QuestType type) {
        LocalDate now = LocalDate.now();

        switch (type) {
            case DAILY:
                return now.format(DateTimeFormatter.ISO_LOCAL_DATE);  // "2026-01-29"

            case WEEKLY:
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
                return now.getYear() + "-W" + String.format("%02d", weekNumber);  // "2026-W05"

            case MONTHLY:
                return now.format(DateTimeFormatter.ofPattern("yyyy-MM"));  // "2026-01"

            case PROGRESSION:
            case EVENT:
                return "once";  // One-time quests

            default:
                return "unknown";
        }
    }

    private Instant calculateExpiresAt(QuestType type) {
        LocalDate now = LocalDate.now();

        switch (type) {
            case DAILY:
                return now.plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

            case WEEKLY:
                return now.plusWeeks(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

            case MONTHLY:
                return now.plusMonths(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

            case PROGRESSION:
            case EVENT:
                return null;  // No expiration

            default:
                return null;
        }
    }
}