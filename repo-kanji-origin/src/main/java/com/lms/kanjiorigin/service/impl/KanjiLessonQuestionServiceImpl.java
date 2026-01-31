package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import com.lms.kanjiorigin.mapper.KanjiLessonQuestionMapper;
import com.lms.kanjiorigin.repository.KanjiLessonQuestionRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.service.KanjiLessonQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonQuestionServiceImpl implements KanjiLessonQuestionService {

    private final KanjiLessonQuestionRepository questionRepository;
    private final KanjiLessonRepository kanjiLessonRepository;
    private final KanjiLessonQuestionMapper questionMapper;

    @Override
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        log.info("Creating question for lesson: {}", request.getKanjiLessonId());

        KanjiLesson lesson = kanjiLessonRepository.findById(request.getKanjiLessonId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + request.getKanjiLessonId()));

        if (request.getWrongOptions().contains(request.getCorrectAnswer())) {
            throw new ApiException(ErrorCode.E227, "Wrong options cannot contain the correct answer");
        }

        KanjiLessonQuestion question = questionMapper.toEntity(request);
        question.setKanjiLesson(lesson);

        KanjiLessonQuestion savedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

    @Override
    public QuestionResponse updateQuestion(String id, UpdateQuestionRequest request) {
        log.info("Updating question with id: {}", id);

        KanjiLessonQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + id));

        if (request.getWrongOptions().contains(request.getCorrectAnswer())) {
            throw new ApiException(ErrorCode.E227, "Wrong options cannot contain the correct answer");
        }

        questionMapper.updateEntity(question, request);
        KanjiLessonQuestion savedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(String id) {
        log.info("Deleting question with id: {}", id);

        KanjiLessonQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + id));

        question.setDeleted(true);
        questionRepository.save(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByLesson(String lessonId) {
        log.info("Getting questions for lesson: {}", lessonId);

        if (!kanjiLessonRepository.existsById(lessonId)) {
            throw new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId);
        }

        List<KanjiLessonQuestion> questions = questionRepository.findByKanjiLessonIdAndDeletedFalse(lessonId);
        return questionMapper.toResponseList(questions);
    }
}
