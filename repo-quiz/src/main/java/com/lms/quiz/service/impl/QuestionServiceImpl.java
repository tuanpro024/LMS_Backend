package com.lms.quiz.service.impl;

import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.dto.response.QuestionResponse;
import com.lms.quiz.entity.Quiz;
import com.lms.quiz.entity.QuizQuestion;
import com.lms.quiz.mapper.QuestionMapper;
import com.lms.quiz.mapper.QuizMapper;
import com.lms.quiz.repository.QuizQuestionRepository;
import com.lms.quiz.repository.QuizRepository;
import com.lms.quiz.service.IQuestionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements IQuestionService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuestionMapper questionMapper;
    private final QuizMapper quizMapper;

    @Override
    @Transactional
    public QuestionResponse addQuestion(String quizId, CreateQuestionRequest request, String userId) {
        log.info("Adding question to quiz {}", quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        int nextIndex = (int) quizQuestionRepository.countByQuizId(quizId);
        QuizQuestion question = questionMapper.toEntity(request, quiz, nextIndex);

        question = quizQuestionRepository.save(question);
        return quizMapper.mapQuestionForAdmin(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByQuizId(String quizId) {
        return quizQuestionRepository.findByQuizIdOrderByQuestionIndexAsc(quizId)
                .stream()
                .map(quizMapper::mapQuestionForAdmin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(String questionId, CreateQuestionRequest request, String userId) {
        log.info("Updating question {}", questionId);

        QuizQuestion existing = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));

        Quiz quiz = existing.getQuiz();
        int index = existing.getQuestionIndex();

        // Clear old sub-entities
        existing.getOptions().clear();
        existing.getBlanks().clear();
        existing.getMatchingPairs().clear();
        existing.getSentenceChunks().clear();

        // Rebuild using mapper
        QuizQuestion updated = questionMapper.toEntity(request, quiz, index);
        updated.setId(existing.getId());

        updated = quizQuestionRepository.save(updated);
        return quizMapper.mapQuestionForAdmin(updated);
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId, String userId) {
        log.info("Deleting question {}", questionId);
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));
        quizQuestionRepository.delete(question);
    }
}
