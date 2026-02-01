package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.dto.request.AssignQuestionToLessonRequest;
import com.lms.kanjiorigin.dto.request.AssignQuestionToMultipleLessonsRequest;
import com.lms.kanjiorigin.dto.response.LessonQuestionAssignmentResponse;
import com.lms.kanjiorigin.dto.response.QuestionResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import com.lms.kanjiorigin.entity.KanjiQuestion;
import com.lms.kanjiorigin.entity.KanjiQuestionWrongOption;
import com.lms.kanjiorigin.repository.KanjiLessonQuestionRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiQuestionRepository;
import com.lms.kanjiorigin.service.KanjiLessonQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonQuestionServiceImpl implements KanjiLessonQuestionService {

    private final KanjiLessonQuestionRepository lessonQuestionRepository;
    private final KanjiLessonRepository lessonRepository;
    private final KanjiQuestionRepository questionRepository;

    @Override
    public LessonQuestionAssignmentResponse assignQuestionToLesson(AssignQuestionToLessonRequest request) {
        log.info("Assigning question {} to lesson {}", request.getKanjiQuestionId(), request.getKanjiLessonId());

        KanjiLesson lesson = lessonRepository.findById(request.getKanjiLessonId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Lesson not found with id: " + request.getKanjiLessonId()));

        KanjiQuestion question = questionRepository.findByIdAndDeletedFalse(request.getKanjiQuestionId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + request.getKanjiQuestionId()));

        if (lessonQuestionRepository.existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(
                request.getKanjiLessonId(), request.getKanjiQuestionId())) {
            throw new ApiException(ErrorCode.E227, "Question is already assigned to this lesson");
        }

        if (lessonQuestionRepository.existsByKanjiLessonIdAndContentIndexAndDeletedFalse(
                request.getKanjiLessonId(), request.getContentIndex())) {
            throw new ApiException(ErrorCode.E227, "Content index already exists in this lesson: " + request.getContentIndex());
        }

        KanjiLessonQuestion assignment = KanjiLessonQuestion.builder()
                .kanjiLesson(lesson)
                .kanjiQuestion(question)
                .contentIndex(request.getContentIndex())
                .build();

        KanjiLessonQuestion saved = lessonQuestionRepository.save(assignment);
        return toResponse(saved);
    }

    @Override
    public List<LessonQuestionAssignmentResponse> assignQuestionToMultipleLessons(AssignQuestionToMultipleLessonsRequest request) {
        log.info("Assigning question {} to {} lessons", request.getKanjiQuestionId(), request.getLessonAssignments().size());

        KanjiQuestion question = questionRepository.findByIdAndDeletedFalse(request.getKanjiQuestionId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Question not found with id: " + request.getKanjiQuestionId()));

        List<LessonQuestionAssignmentResponse> results = new ArrayList<>();

        for (AssignQuestionToMultipleLessonsRequest.LessonAssignment lessonAssignment : request.getLessonAssignments()) {
            KanjiLesson lesson = lessonRepository.findById(lessonAssignment.getKanjiLessonId())
                    .orElseThrow(() -> new ApiException(ErrorCode.E227, "Lesson not found with id: " + lessonAssignment.getKanjiLessonId()));

            if (lessonQuestionRepository.existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(
                    lessonAssignment.getKanjiLessonId(), request.getKanjiQuestionId())) {
                throw new ApiException(ErrorCode.E227, "Question is already assigned to lesson: " + lessonAssignment.getKanjiLessonId());
            }

            if (lessonQuestionRepository.existsByKanjiLessonIdAndContentIndexAndDeletedFalse(
                    lessonAssignment.getKanjiLessonId(), lessonAssignment.getContentIndex())) {
                throw new ApiException(ErrorCode.E227, "Content index " + lessonAssignment.getContentIndex() + " already exists in lesson: " + lessonAssignment.getKanjiLessonId());
            }

            KanjiLessonQuestion assignment = KanjiLessonQuestion.builder()
                    .kanjiLesson(lesson)
                    .kanjiQuestion(question)
                    .contentIndex(lessonAssignment.getContentIndex())
                    .build();

            KanjiLessonQuestion saved = lessonQuestionRepository.save(assignment);
            results.add(toResponse(saved));
        }

        return results;
    }

    @Override
    public void removeQuestionFromLesson(String assignmentId) {
        log.info("Removing assignment with id: {}", assignmentId);

        KanjiLessonQuestion assignment = lessonQuestionRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Assignment not found with id: " + assignmentId));

        assignment.setDeleted(true);
        lessonQuestionRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonQuestionAssignmentResponse> getQuestionsByLesson(String lessonId) {
        log.info("Getting questions for lesson: {}", lessonId);

        if (!lessonRepository.existsById(lessonId)) {
            throw new ApiException(ErrorCode.E227, "Lesson not found with id: " + lessonId);
        }

        List<KanjiLessonQuestion> assignments = lessonQuestionRepository.findByKanjiLessonIdAndDeletedFalse(lessonId);
        return assignments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LessonQuestionAssignmentResponse toResponse(KanjiLessonQuestion assignment) {
        KanjiQuestion q = assignment.getKanjiQuestion();
        QuestionResponse questionResponse = QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .correctAnswer(q.getCorrectAnswer())
                .wrongOptions(q.getWrongOptions().stream()
                        .map(KanjiQuestionWrongOption::getWrongOption)
                        .collect(Collectors.toList()))
                .build();

        return LessonQuestionAssignmentResponse.builder()
                .id(assignment.getId())
                .kanjiLessonId(assignment.getKanjiLesson().getId())
                .kanjiQuestionId(q.getId())
                .contentIndex(assignment.getContentIndex())
                .question(questionResponse)
                .build();
    }
}
