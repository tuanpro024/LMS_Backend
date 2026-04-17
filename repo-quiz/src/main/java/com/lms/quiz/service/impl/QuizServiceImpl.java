package com.lms.quiz.service.impl;

import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.dto.request.CreateQuizRequest;
import com.lms.quiz.dto.response.QuizDetailResponse;
import com.lms.quiz.dto.response.QuizResponse;
import com.lms.quiz.entity.Quiz;
import com.lms.quiz.entity.QuizQuestion;
import com.lms.quiz.mapper.QuestionMapper;
import com.lms.quiz.mapper.QuizMapper;
import com.lms.quiz.repository.QuizRepository;
import com.lms.quiz.service.IQuizService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements IQuizService {

    private final QuizRepository quizRepository;
    private final StudySetRepository studySetRepository;
    private final StudySetApiDelegate studySetApiDelegate;
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuizDetailResponse createQuiz(CreateQuizRequest request, String userId) {
        log.info("Creating quiz '{}' for studySet {}", request.getTitle(), request.getStudySetId());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new EntityNotFoundException("StudySet not found: " + request.getStudySetId()));

        long existingCount = quizRepository.countByStudySetId(request.getStudySetId());

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instruction(request.getInstruction())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty()
                        : com.lms.quiz.entity.enums.DifficultyLevel.MEDIUM)
                .timeLimitSeconds(request.getTimeLimitSeconds() != null ? request.getTimeLimitSeconds() : 0)
                .passingScore(request.getPassingScore() != null ? request.getPassingScore() : 70)
                .shuffleQuestions(request.getShuffleQuestions() != null ? request.getShuffleQuestions() : true)
                .questions(new ArrayList<>())
                .build();

        quiz.setStudySet(studySet);
        quiz.setContentIndex((int) existingCount);

        // Add questions if provided
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            IntStream.range(0, request.getQuestions().size()).forEach(i -> {
                CreateQuestionRequest qReq = request.getQuestions().get(i);
                QuizQuestion question = questionMapper.toEntity(qReq, quiz, i);
                quiz.getQuestions().add(question);
            });
        }

        Quiz saved = quizRepository.save(quiz);
        studySetApiDelegate.revertParentPackagesToDraft(studySet.getId(), userId);
        log.info("Created quiz with ID: {}", saved.getId());

        return quizMapper.toQuizDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizById(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));
        return quizMapper.toQuizDetailResponse(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizForAttempt(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));
        return quizMapper.toQuizDetailResponseForAttempt(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByStudySetId(String studySetId) {
        return quizRepository.findActiveByStudySetId(studySetId)
                .stream()
                .map(quizMapper::toQuizResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizDetailResponse updateQuiz(String quizId, CreateQuizRequest request, String userId) {
        log.info("Updating quiz {}", quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setInstruction(request.getInstruction());

        if (request.getDifficulty() != null)
            quiz.setDifficulty(request.getDifficulty());
        if (request.getTimeLimitSeconds() != null)
            quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());
        if (request.getPassingScore() != null)
            quiz.setPassingScore(request.getPassingScore());
        if (request.getShuffleQuestions() != null)
            quiz.setShuffleQuestions(request.getShuffleQuestions());

        // If questions are provided, replace all
        if (request.getQuestions() != null) {
            quiz.getQuestions().clear();
            IntStream.range(0, request.getQuestions().size()).forEach(i -> {
                CreateQuestionRequest qReq = request.getQuestions().get(i);
                QuizQuestion question = questionMapper.toEntity(qReq, quiz, i);
                quiz.getQuestions().add(question);
            });
        }

        Quiz saved = quizRepository.save(quiz);
        if (quiz.getStudySet() != null) {
            studySetApiDelegate.revertParentPackagesToDraft(quiz.getStudySet().getId(), userId);
        }
        return quizMapper.toQuizDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteQuiz(String quizId, String userId) {
        log.info("Deleting quiz {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));
        String studySetId = quiz.getStudySet() != null ? quiz.getStudySet().getId() : null;
        quizRepository.delete(quiz);
        studySetApiDelegate.revertParentPackagesToDraft(studySetId, userId);
    }
}
