package com.lms.quiz.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.quiz.dto.request.CheckQuestionRequest;
import com.lms.quiz.dto.request.SubmitQuizRequest;
import com.lms.quiz.dto.request.UpdateQuestionResultRequest;
import com.lms.quiz.dto.response.CheckQuestionResponse;
import com.lms.quiz.dto.response.QuizProgressResponse;
import com.lms.quiz.dto.response.QuizResultResponse;
import com.lms.quiz.entity.QuizAttempt;
import com.lms.quiz.entity.UserQuizProgress;
import com.lms.quiz.entity.MatchingPair;
import com.lms.quiz.entity.Quiz;
import com.lms.quiz.entity.QuizBlank;
import com.lms.quiz.entity.QuizOption;
import com.lms.quiz.entity.QuizQuestion;
import com.lms.quiz.entity.SentenceChunk;
import com.lms.quiz.entity.UserQuizStudySetProgress;
import com.lms.quiz.entity.enums.QuestionType;
import com.lms.quiz.entity.enums.StudySetProgressStatus;
import com.lms.quiz.event.QuizAttemptSubmittedEvent;
import com.lms.quiz.event.QuizStudySetProgressUpdatedEvent;
import com.lms.quiz.repository.QuizAttemptRepository;
import com.lms.quiz.repository.QuizRepository;
import com.lms.quiz.repository.UserQuizProgressRepository;
import com.lms.quiz.repository.UserQuizStudySetProgressRepository;
import com.lms.quiz.service.IQuizAttemptService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptServiceImpl implements IQuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserQuizProgressRepository userQuizProgressRepository;
    private final UserQuizStudySetProgressRepository studySetProgressRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(SubmitQuizRequest request, String userId) {
        log.info("User {} submitting quiz {}", userId, request.getQuizId());

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + request.getQuizId()));

        // Build answer map: questionId -> SubmitAnswerRequest
        Map<String, SubmitQuizRequest.SubmitAnswerRequest> answerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (SubmitQuizRequest.SubmitAnswerRequest answer : request.getAnswers()) {
                answerMap.put(answer.getQuestionId(), answer);
            }
        }

        int totalPoints = 0;
        int earnedPoints = 0;
        int correctCount = 0;
        List<QuizResultResponse.QuestionResult> results = new ArrayList<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            totalPoints += question.getPoints();

            SubmitQuizRequest.SubmitAnswerRequest userAnswer = answerMap.get(question.getId());
            boolean isCorrect = false;
            Object correctAnswerObj = null;
            Object userAnswerObj = null;

            if (userAnswer != null) {
                switch (question.getQuestionType()) {
                    case MULTIPLE_CHOICE -> {
                        GradeResult mcResult = gradeMultipleChoice(question, userAnswer);
                        isCorrect = mcResult.correct;
                        correctAnswerObj = mcResult.correctAnswer;
                        userAnswerObj = userAnswer.getSelectedOptionId();
                    }
                    case FILL_IN_BLANK -> {
                        GradeResult fibResult = gradeFillInBlank(question, userAnswer);
                        isCorrect = fibResult.correct;
                        correctAnswerObj = fibResult.correctAnswer;
                        userAnswerObj = userAnswer.getBlankAnswers();
                    }
                    case MATCHING_PAIRS -> {
                        GradeResult matchResult = gradeMatchingPairs(question, userAnswer);
                        isCorrect = matchResult.correct;
                        correctAnswerObj = matchResult.correctAnswer;
                        userAnswerObj = userAnswer.getMatchAnswers();
                    }
                    case SENTENCE_BUILDER -> {
                        GradeResult sbResult = gradeSentenceBuilder(question, userAnswer);
                        isCorrect = sbResult.correct;
                        correctAnswerObj = sbResult.correctAnswer;
                        userAnswerObj = userAnswer.getOrderedChunkIds();
                    }
                }
            }

            int pointsEarned = isCorrect ? question.getPoints() : 0;
            earnedPoints += pointsEarned;
            if (isCorrect)
                correctCount++;

            results.add(QuizResultResponse.QuestionResult.builder()
                    .questionId(question.getId())
                    .questionType(question.getQuestionType().name())
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .explanation(question.getExplanation())
                    .correctAnswer(correctAnswerObj)
                    .userAnswer(userAnswerObj)
                    .build());
        }

        double scorePercentage = totalPoints > 0 ? (double) earnedPoints / totalPoints * 100 : 0;
        double roundedScore = Math.round(scorePercentage * 100.0) / 100.0;
        boolean passed = scorePercentage >= quiz.getPassingScore();

        Instant now = Instant.now();
        QuizAttempt attempt = quizAttemptRepository.save(QuizAttempt.builder()
                .userId(userId)
                .quizId(quiz.getId())
                .studySetId(quiz.getStudySet().getId())
                .totalQuestions(quiz.getQuestions().size())
                .correctAnswers(correctCount)
                .totalPoints(totalPoints)
                .earnedPoints(earnedPoints)
                .scorePercentage(roundedScore)
                .passed(passed)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .submittedAt(now)
                .build());

        UserQuizProgress progress = userQuizProgressRepository.findByUserIdAndQuizId(userId, quiz.getId())
                .orElseGet(() -> UserQuizProgress.builder()
                        .userId(userId)
                        .quizId(quiz.getId())
                        .studySetId(quiz.getStudySet().getId())
                        .attemptsCount(0)
                        .latestScorePercentage(0.0)
                        .bestScorePercentage(0.0)
                        .completed(false)
                        .build());

        int attemptsCount = progress.getAttemptsCount() == null ? 0 : progress.getAttemptsCount();
        progress.setAttemptsCount(attemptsCount + 1);
        progress.setLatestScorePercentage(roundedScore);
        progress.setLatestAttemptId(attempt.getId());

        if (progress.getFirstAttemptAt() == null) {
            progress.setFirstAttemptAt(now);
        }
        progress.setLastAttemptAt(now);

        if (progress.getBestScorePercentage() == null || roundedScore >= progress.getBestScorePercentage()) {
            progress.setBestScorePercentage(roundedScore);
            progress.setBestAttemptId(attempt.getId());
        }

        if (passed) {
            progress.setCompleted(true);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(now);
            }
        }

        progress = userQuizProgressRepository.save(progress);

        // Recalculate Study Set Progress
        String studySetId = quiz.getStudySet().getId();
        UserQuizStudySetProgress studySetProgress = studySetProgressRepository.findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> UserQuizStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .completedQuizzes(0)
                        .totalQuizzes(0)
                        .progressPercentage(0.0)
                        .status(StudySetProgressStatus.NOT_STARTED)
                        .build());

        if (studySetProgress.getFirstStartedAt() == null) {
            studySetProgress.setFirstStartedAt(now);
        }

        long totalQuizzes = quizRepository.countByStudySetId(studySetId);
        long completedQuizzes = userQuizProgressRepository.countByUserIdAndStudySetIdAndCompletedTrue(userId, studySetId);

        studySetProgress.setTotalQuizzes((int) totalQuizzes);
        studySetProgress.setCompletedQuizzes((int) completedQuizzes);

        double studySetProgressPercentage = totalQuizzes > 0 ? (double) completedQuizzes / totalQuizzes * 100 : 0;
        studySetProgress.setProgressPercentage(Math.round(studySetProgressPercentage * 100.0) / 100.0);

        if (completedQuizzes == 0) {
            studySetProgress.setStatus(StudySetProgressStatus.NOT_STARTED);
        } else if (completedQuizzes >= totalQuizzes && totalQuizzes > 0) {
            studySetProgress.setStatus(StudySetProgressStatus.COMPLETED);
            if (studySetProgress.getCompletedAt() == null) {
                studySetProgress.setCompletedAt(now);
            }
        } else {
            studySetProgress.setStatus(StudySetProgressStatus.IN_PROGRESS);
        }

        studySetProgress = studySetProgressRepository.save(studySetProgress);

        // Publish study set event
        eventPublisher.publishEvent(QuizStudySetProgressUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .studySetId(studySetId)
                .completedQuizzes(studySetProgress.getCompletedQuizzes())
                .totalQuizzes(studySetProgress.getTotalQuizzes())
                .progressPercentage(studySetProgress.getProgressPercentage())
                .completed(StudySetProgressStatus.COMPLETED.equals(studySetProgress.getStatus()))
                .completedAt(studySetProgress.getCompletedAt())
                .occurredAt(now)
                .build());

        eventPublisher.publishEvent(QuizAttemptSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .attemptId(attempt.getId())
                .userId(userId)
                .studySetId(quiz.getStudySet().getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .attemptsCount(progress.getAttemptsCount())
                .earnedPoints(earnedPoints)
                .totalPoints(totalPoints)
                .scorePercentage(roundedScore)
                .passed(passed)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .occurredAt(now)
                .build());

        return QuizResultResponse.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(quiz.getQuestions().size())
                .correctAnswers(correctCount)
                .totalPoints(totalPoints)
                .earnedPoints(earnedPoints)
                .scorePercentage(roundedScore)
                .passed(passed)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .questionResults(results)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckQuestionResponse checkQuestion(CheckQuestionRequest request, String userId) {
        log.info("User {} checking question {} in quiz {}", userId,
                request != null && request.getAnswer() != null ? request.getAnswer().getQuestionId() : null,
                request != null ? request.getQuizId() : null);

        if (request == null || request.getQuizId() == null || request.getAnswer() == null
                || request.getAnswer().getQuestionId() == null) {
            throw new IllegalArgumentException("quizId and answer.questionId are required");
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + request.getQuizId()));

        QuizQuestion question = quiz.getQuestions().stream()
                .filter(q -> request.getAnswer().getQuestionId().equals(q.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Question not found in quiz: " + request.getAnswer().getQuestionId()));

        GradeResult gradeResult;
        Object userAnswerObj;

        switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE -> {
                gradeResult = gradeMultipleChoice(question, request.getAnswer());
                userAnswerObj = request.getAnswer().getSelectedOptionId();
            }
            case FILL_IN_BLANK -> {
                gradeResult = gradeFillInBlank(question, request.getAnswer());
                userAnswerObj = request.getAnswer().getBlankAnswers();
            }
            case MATCHING_PAIRS -> {
                gradeResult = gradeMatchingPairs(question, request.getAnswer());
                userAnswerObj = request.getAnswer().getMatchAnswers();
            }
            case SENTENCE_BUILDER -> {
                gradeResult = gradeSentenceBuilder(question, request.getAnswer());
                userAnswerObj = request.getAnswer().getOrderedChunkIds();
            }
            default -> throw new IllegalArgumentException("Unsupported question type: " + question.getQuestionType());
        }

        int maxPoints = question.getPoints() != null ? question.getPoints() : 0;
        int pointsEarned = gradeResult.correct ? maxPoints : 0;

        return CheckQuestionResponse.builder()
                .quizId(quiz.getId())
                .questionId(question.getId())
                .questionType(question.getQuestionType().name())
                .isCorrect(gradeResult.correct)
                .pointsEarned(pointsEarned)
                .maxPoints(maxPoints)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .explanation(question.getExplanation())
                .correctAnswer(gradeResult.correctAnswer)
                .userAnswer(userAnswerObj)
                .build();
    }

    @Override
    @Transactional
    public QuizResultResponse updateQuestionResult(UpdateQuestionResultRequest request, String userId) {
        if (request == null || request.getQuizId() == null || request.getAnswer() == null
                || request.getAnswer().getQuestionId() == null) {
            throw new IllegalArgumentException("quizId and answer.questionId are required");
        }

        Map<String, SubmitQuizRequest.SubmitAnswerRequest> answerMap = new LinkedHashMap<>();
        if (request.getPreviousQuestionResults() != null) {
            for (UpdateQuestionResultRequest.PreviousQuestionResult item : request.getPreviousQuestionResults()) {
                if (item == null || item.getQuestionId() == null || item.getQuestionType() == null) {
                    continue;
                }
                SubmitQuizRequest.SubmitAnswerRequest converted = convertPreviousUserAnswer(item);
                if (converted != null) {
                    answerMap.put(converted.getQuestionId(), converted);
                }
            }
        }

        answerMap.put(request.getAnswer().getQuestionId(), request.getAnswer());

        SubmitQuizRequest reconstructed = SubmitQuizRequest.builder()
                .quizId(request.getQuizId())
                .answers(new ArrayList<>(answerMap.values()))
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .build();

        return submitQuiz(reconstructed, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizProgressResponse getQuizProgress(String quizId, String userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        UserQuizProgress progress = userQuizProgressRepository.findByUserIdAndQuizId(userId, quizId)
                .orElseGet(() -> UserQuizProgress.builder()
                        .userId(userId)
                        .quizId(quizId)
                        .studySetId(quiz.getStudySet().getId())
                        .attemptsCount(0)
                        .latestScorePercentage(0.0)
                        .bestScorePercentage(0.0)
                        .completed(false)
                        .build());

        return QuizProgressResponse.builder()
                .userId(progress.getUserId())
                .quizId(progress.getQuizId())
                .studySetId(progress.getStudySetId())
                .attemptsCount(progress.getAttemptsCount())
                .latestScorePercentage(progress.getLatestScorePercentage())
                .bestScorePercentage(progress.getBestScorePercentage())
                .completed(progress.getCompleted())
                .firstAttemptAt(progress.getFirstAttemptAt())
                .lastAttemptAt(progress.getLastAttemptAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    // ============ Grading Logic ============

    private GradeResult gradeMultipleChoice(QuizQuestion question, SubmitQuizRequest.SubmitAnswerRequest answer) {
        String selectedId = answer.getSelectedOptionId();
        QuizOption correctOption = question.getOptions().stream()
                .filter(QuizOption::getIsCorrect)
                .findFirst()
                .orElse(null);

        String correctId = correctOption != null ? correctOption.getId() : null;
        boolean isCorrect = correctId != null && correctId.equals(selectedId);

        return new GradeResult(isCorrect, correctId);
    }

    private GradeResult gradeFillInBlank(QuizQuestion question, SubmitQuizRequest.SubmitAnswerRequest answer) {
        if (answer.getBlankAnswers() == null || question.getBlanks().isEmpty()) {
            return new GradeResult(false, null);
        }

        Map<Integer, String> correctAnswers = new HashMap<>();
        boolean allCorrect = true;

        for (QuizBlank blank : question.getBlanks()) {
            correctAnswers.put(blank.getBlankIndex(), blank.getCorrectAnswer());

            SubmitQuizRequest.BlankAnswer userBlank = answer.getBlankAnswers().stream()
                    .filter(ba -> ba.getBlankIndex() != null && ba.getBlankIndex().equals(blank.getBlankIndex()))
                    .findFirst()
                    .orElse(null);

            if (userBlank == null || userBlank.getAnswer() == null) {
                allCorrect = false;
                continue;
            }

            String userText = userBlank.getAnswer().trim().toLowerCase();
            String correctText = blank.getCorrectAnswer().trim().toLowerCase();

            if (!userText.equals(correctText)) {
                // Check accepted answers
                boolean matchedAccepted = false;
                if (blank.getAcceptedAnswers() != null) {
                    try {
                        List<String> accepted = objectMapper.readValue(
                                blank.getAcceptedAnswers(), new TypeReference<List<String>>() {
                                });
                        matchedAccepted = accepted.stream()
                                .anyMatch(a -> a.trim().toLowerCase().equals(userText));
                    } catch (Exception e) {
                        log.warn("Failed to parse acceptedAnswers JSON: {}", e.getMessage());
                    }
                }
                if (!matchedAccepted) {
                    allCorrect = false;
                }
            }
        }

        return new GradeResult(allCorrect, correctAnswers);
    }

    private GradeResult gradeMatchingPairs(QuizQuestion question, SubmitQuizRequest.SubmitAnswerRequest answer) {
        if (answer.getMatchAnswers() == null || question.getMatchingPairs().isEmpty()) {
            return new GradeResult(false, null);
        }

        // Build correct mapping: each MatchingPair's ID maps prompt→answer
        // User submits pairs of (promptId, answerId) which should be the same pair ID
        Map<String, String> correctPairs = new HashMap<>();
        for (MatchingPair pair : question.getMatchingPairs()) {
            correctPairs.put(pair.getId(), pair.getId());
        }

        boolean allCorrect = true;
        for (SubmitQuizRequest.MatchAnswer ma : answer.getMatchAnswers()) {
            if (!ma.getPromptId().equals(ma.getAnswerId())) {
                allCorrect = false;
                break;
            }
            if (!correctPairs.containsKey(ma.getPromptId())) {
                allCorrect = false;
                break;
            }
        }

        // Also check count matches
        if (answer.getMatchAnswers().size() != question.getMatchingPairs().size()) {
            allCorrect = false;
        }

        return new GradeResult(allCorrect, correctPairs);
    }

    private GradeResult gradeSentenceBuilder(QuizQuestion question, SubmitQuizRequest.SubmitAnswerRequest answer) {
        if (answer.getOrderedChunkIds() == null || question.getSentenceChunks().isEmpty()) {
            return new GradeResult(false, null);
        }

        // Get correct order: only non-distractor chunks, ordered by correctPosition
        List<String> correctOrder = question.getSentenceChunks().stream()
                .filter(c -> !c.getIsDistractor())
                .sorted(Comparator.comparingInt(SentenceChunk::getCorrectPosition))
                .map(SentenceChunk::getId)
                .collect(Collectors.toList());

        // Filter user's answer to remove distractors
        Set<String> distractorIds = question.getSentenceChunks().stream()
                .filter(SentenceChunk::getIsDistractor)
                .map(SentenceChunk::getId)
                .collect(Collectors.toSet());

        List<String> userOrder = answer.getOrderedChunkIds().stream()
                .filter(id -> !distractorIds.contains(id))
                .collect(Collectors.toList());

        boolean isCorrect = correctOrder.equals(userOrder);

        return new GradeResult(isCorrect, correctOrder);
    }

    private SubmitQuizRequest.SubmitAnswerRequest convertPreviousUserAnswer(
            UpdateQuestionResultRequest.PreviousQuestionResult item) {
        QuestionType questionType;
        try {
            questionType = QuestionType.valueOf(item.getQuestionType());
        } catch (Exception e) {
            return null;
        }

        SubmitQuizRequest.SubmitAnswerRequest.SubmitAnswerRequestBuilder builder = SubmitQuizRequest.SubmitAnswerRequest
                .builder()
                .questionId(item.getQuestionId());

        Object userAnswer = item.getUserAnswer();
        switch (questionType) {
            case MULTIPLE_CHOICE -> {
                String selected = asString(userAnswer);
                if (selected != null && !selected.isBlank()) {
                    builder.selectedOptionId(selected);
                }
            }
            case FILL_IN_BLANK -> builder.blankAnswers(parseBlankAnswers(userAnswer));
            case MATCHING_PAIRS -> builder.matchAnswers(parseMatchAnswers(userAnswer));
            case SENTENCE_BUILDER -> builder.orderedChunkIds(parseOrderedChunkIds(userAnswer));
        }

        return builder.build();
    }

    private List<SubmitQuizRequest.BlankAnswer> parseBlankAnswers(Object userAnswer) {
        if (userAnswer == null) {
            return Collections.emptyList();
        }

        List<SubmitQuizRequest.BlankAnswer> answers = new ArrayList<>();
        if (userAnswer instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Integer idx = asInteger(entry.getKey());
                String val = asString(entry.getValue());
                if (idx != null && val != null) {
                    answers.add(SubmitQuizRequest.BlankAnswer.builder().blankIndex(idx).answer(val).build());
                }
            }
        } else if (userAnswer instanceof Collection<?> collection) {
            for (Object obj : collection) {
                if (!(obj instanceof Map<?, ?> item)) {
                    continue;
                }
                Integer idx = asInteger(item.get("blankIndex"));
                if (idx == null) {
                    idx = asInteger(item.get("index"));
                }
                String val = asString(item.get("answer"));
                if (val == null) {
                    val = asString(item.get("correctAnswer"));
                }
                if (idx != null && val != null) {
                    answers.add(SubmitQuizRequest.BlankAnswer.builder().blankIndex(idx).answer(val).build());
                }
            }
        }

        answers.sort(Comparator.comparingInt(SubmitQuizRequest.BlankAnswer::getBlankIndex));
        return answers;
    }

    private List<SubmitQuizRequest.MatchAnswer> parseMatchAnswers(Object userAnswer) {
        if (!(userAnswer instanceof Collection<?> collection)) {
            return Collections.emptyList();
        }

        List<SubmitQuizRequest.MatchAnswer> answers = new ArrayList<>();
        for (Object obj : collection) {
            if (!(obj instanceof Map<?, ?> item)) {
                continue;
            }
            String promptId = asString(item.get("promptId"));
            String answerId = asString(item.get("answerId"));
            if (promptId != null && answerId != null) {
                answers.add(SubmitQuizRequest.MatchAnswer.builder()
                        .promptId(promptId)
                        .answerId(answerId)
                        .build());
            }
        }
        return answers;
    }

    private List<String> parseOrderedChunkIds(Object userAnswer) {
        if (!(userAnswer instanceof Collection<?> collection)) {
            return Collections.emptyList();
        }

        List<String> chunkIds = new ArrayList<>();
        for (Object obj : collection) {
            String value = asString(obj);
            if (value != null && !value.isBlank()) {
                chunkIds.add(value);
            }
        }
        return chunkIds;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        return String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private record GradeResult(boolean correct, Object correctAnswer) {
    }
}
