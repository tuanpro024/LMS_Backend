package com.lms.quiz.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.quiz.dto.request.SubmitQuizRequest;
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
import com.lms.quiz.event.QuizAttemptSubmittedEvent;
import com.lms.quiz.repository.QuizAttemptRepository;
import com.lms.quiz.repository.QuizRepository;
import com.lms.quiz.repository.UserQuizProgressRepository;
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

    private record GradeResult(boolean correct, Object correctAnswer) {
    }
}
