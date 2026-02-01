package com.lms.kanjiorigin.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.dto.request.CreateQuestionRequest;
import com.lms.kanjiorigin.dto.request.KanjiQuestionSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateQuestionRequest;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import com.lms.kanjiorigin.entity.KanjiQuestion;
import com.lms.kanjiorigin.entity.KanjiQuestionWrongOption;
import com.lms.kanjiorigin.repository.KanjiLessonQuestionRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiQuestionRepository;
import com.lms.kanjiorigin.service.KanjiQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiQuestionServiceImpl implements KanjiQuestionService {

    private final KanjiQuestionRepository questionRepository;
    private final KanjiLessonRepository lessonRepository;
    private final KanjiLessonQuestionRepository lessonQuestionRepository;

    @Override
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        log.info("Creating question with content: {}", request.getContent());

        if (questionRepository.existsByContentAndDeletedFalse(request.getContent())) {
            throw new ApiException(ErrorCode.E227, "Question with this content already exists");
        }

        if (request.getWrongOptions().contains(request.getCorrectAnswer())) {
            throw new ApiException(ErrorCode.E227, "Wrong options cannot contain the correct answer");
        }

        KanjiQuestion question = KanjiQuestion.builder()
                .content(request.getContent())
                .correctAnswer(request.getCorrectAnswer())
                .build();

        List<KanjiQuestionWrongOption> wrongOptions = request.getWrongOptions().stream()
                .map(opt -> KanjiQuestionWrongOption.builder()
                        .wrongOption(opt)
                        .kanjiQuestion(question)
                        .build())
                .collect(Collectors.toList());
        question.setWrongOptions(wrongOptions);

        KanjiQuestion savedQuestion = questionRepository.save(question);

        if (request.getLessonAssignments() != null && !request.getLessonAssignments().isEmpty()) {
            for (CreateQuestionRequest.LessonAssignment assignment : request.getLessonAssignments()) {
                assignToLesson(savedQuestion, assignment.getKanjiLessonId(), assignment.getContentIndex());
            }
        }

        return toResponse(savedQuestion);
    }

    private void assignToLesson(KanjiQuestion question, String lessonId, Integer contentIndex) {
        KanjiLesson lesson = lessonRepository.findByIdAndDeletedFalse(lessonId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Lesson not found: " + lessonId));

        if (lessonQuestionRepository.existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(lessonId, question.getId())) {
            throw new ApiException(ErrorCode.E227, "Question already assigned to lesson: " + lessonId);
        }

        if (lessonQuestionRepository.existsByKanjiLessonIdAndContentIndexAndDeletedFalse(lessonId, contentIndex)) {
            throw new ApiException(ErrorCode.E227, "Content index " + contentIndex + " already exists in lesson: " + lessonId);
        }

        KanjiLessonQuestion lessonQuestion = KanjiLessonQuestion.builder()
                .kanjiLesson(lesson)
                .kanjiQuestion(question)
                .contentIndex(contentIndex)
                .build();
        lessonQuestionRepository.save(lessonQuestion);
        log.info("Assigned question {} to lesson {} at index {}", question.getId(), lessonId, contentIndex);
    }

    @Override
    public QuestionResponse updateQuestion(String id, UpdateQuestionRequest request) {
        log.info("Updating question with id: {}", id);

        KanjiQuestion question = questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + id));

        if (request.getContent() != null && !request.getContent().equals(question.getContent())) {
            if (questionRepository.existsByContentAndIdNotAndDeletedFalse(request.getContent(), id)) {
                throw new ApiException(ErrorCode.E227, "Question with this content already exists");
            }
        }

        String correctAnswer = request.getCorrectAnswer() != null ? request.getCorrectAnswer() : question.getCorrectAnswer();
        if (request.getWrongOptions() != null && request.getWrongOptions().contains(correctAnswer)) {
            throw new ApiException(ErrorCode.E227, "Wrong options cannot contain the correct answer");
        }

        if (request.getContent() != null) {
            question.setContent(request.getContent());
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (request.getWrongOptions() != null) {
            question.getWrongOptions().clear();
            List<KanjiQuestionWrongOption> newWrongOptions = request.getWrongOptions().stream()
                    .map(opt -> KanjiQuestionWrongOption.builder()
                            .wrongOption(opt)
                            .kanjiQuestion(question)
                            .build())
                    .collect(Collectors.toList());
            question.getWrongOptions().addAll(newWrongOptions);
        }

        KanjiQuestion savedQuestion = questionRepository.save(question);
        return toResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(String id) {
        log.info("Deleting question with id: {}", id);

        KanjiQuestion question = questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + id));

        question.setDeleted(true);
        questionRepository.save(question);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(String id) {
        log.info("Getting question with id: {}", id);

        KanjiQuestion question = questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + id));

        return toResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions() {
        log.info("Getting all questions");
        return questionRepository.findAllByDeletedFalse().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByLessonId(String lessonId) {
        log.info("Getting questions for lesson: {}", lessonId);
        
        if (!lessonRepository.existsByIdAndDeletedFalse(lessonId)) {
            throw new ApiException(ErrorCode.E227, "Lesson not found: " + lessonId);
        }
        
        return lessonQuestionRepository.findByKanjiLessonIdAndDeletedFalseOrderByContentIndex(lessonId)
                .stream()
                .map(lq -> toResponse(lq.getKanjiQuestion()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> search(KanjiQuestionSearchRequest request) {
        log.info("Searching kanji questions with keyword: {}", request.getKeyword());
        List<KanjiQuestion> questions = questionRepository.searchList(request.getKeyword());
        return questions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> searchPaged(KanjiQuestionSearchRequest request) {
        log.info("Searching paged kanji questions with keyword: {}", request.getKeyword());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<KanjiQuestion> page = questionRepository.search(request.getKeyword(), pageable);
        
        List<QuestionResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<QuestionResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    private QuestionResponse toResponse(KanjiQuestion question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .content(question.getContent())
                .correctAnswer(question.getCorrectAnswer())
                .wrongOptions(question.getWrongOptions().stream()
                        .map(KanjiQuestionWrongOption::getWrongOption)
                        .collect(Collectors.toList()))
                .build();
    }
}

