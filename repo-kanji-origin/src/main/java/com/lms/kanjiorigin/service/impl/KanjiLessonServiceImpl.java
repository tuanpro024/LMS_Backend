package com.lms.kanjiorigin.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.ImportKanjiRequest;
import com.lms.kanjiorigin.dto.request.ImportLessonRequest;
import com.lms.kanjiorigin.dto.request.ImportQuestionRequest;
import com.lms.kanjiorigin.dto.request.KanjiLessonSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.ImportResultResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.entity.KanjiQuestion;
import com.lms.kanjiorigin.entity.KanjiQuestionWrongOption;
import com.lms.kanjiorigin.mapper.KanjiLessonMapper;
import com.lms.kanjiorigin.repository.KanjiLessonQuestionRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import com.lms.kanjiorigin.repository.KanjiQuestionRepository;
import com.lms.kanjiorigin.repository.KanjiQuestionWrongOptionRepository;
import com.lms.kanjiorigin.service.KanjiLessonService;
import com.lms.kanjiorigin.util.KanjiExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonServiceImpl implements KanjiLessonService {

    private final KanjiLessonRepository kanjiLessonRepository;
    private final StudySetRepository studySetRepository;
    private final KanjiLessonMapper kanjiLessonMapper;
    private final KanjiOriginRepository kanjiOriginRepository;
    private final KanjiQuestionRepository kanjiQuestionRepository;
    private final KanjiQuestionWrongOptionRepository kanjiQuestionWrongOptionRepository;
    private final KanjiLessonQuestionRepository kanjiLessonQuestionRepository;

    @Override
    public ImportResultResponse importFromExcel(String studySetId, MultipartFile file) {
        log.info("Importing kanji lessons from Excel for studySetId: {}", studySetId);
        
        ImportResultResponse response = ImportResultResponse.builder()
                .errors(new ArrayList<>())
                .build();
        
        StudySet studySet = studySetRepository.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found with id: " + studySetId));
        
        List<ImportLessonRequest> lessonRequests;
        try {
            lessonRequests = KanjiExcelHelper.excelToLessons(file.getInputStream());
        } catch (IOException e) {
            log.error("Error parsing Excel file", e);
            throw new ApiException(ErrorCode.E227, "Error parsing Excel file: " + e.getMessage());
        }
        
        response.setTotalSheets(lessonRequests.size());
        
        int lessonsCreated = 0;
        int lessonsUpdated = 0;
        int kanjisCreated = 0;
        int questionsCreated = 0;
        int questionsReused = 0;
        
        for (ImportLessonRequest lessonRequest : lessonRequests) {
            try {
                // Check if lesson already exists
                Optional<KanjiLesson> existingLessonOpt = kanjiLessonRepository
                        .findByStudySetIdAndTitleAndDeletedFalse(studySetId, lessonRequest.getTitle());
                
                KanjiLesson lesson;
                if (existingLessonOpt.isPresent()) {
                    lesson = existingLessonOpt.get();
                    if (lessonRequest.getDescription() != null) {
                        lesson.setDescription(lessonRequest.getDescription());
                    }
                    lesson = kanjiLessonRepository.save(lesson);
                    lessonsUpdated++;
                } else {
                    // Create new lesson
                    Integer maxIndex = kanjiLessonRepository.findMaxContentIndexByStudySetId(studySetId);
                    int newIndex = (maxIndex == null) ? 1 : maxIndex + 1;
                    
                    lesson = new KanjiLesson();
                    lesson.setStudySet(studySet);
                    lesson.setTitle(lessonRequest.getTitle());
                    lesson.setDescription(lessonRequest.getDescription());
                    lesson.setContentIndex(newIndex);
                    lesson.setDeleted(false);
                    lesson = kanjiLessonRepository.save(lesson);
                    lessonsCreated++;
                }
                
                // Process kanjis
                for (ImportKanjiRequest kanjiRequest : lessonRequest.getKanjis()) {
                    try {
                        // Check if kanji already exists
                        Optional<KanjiOrigin> existingKanjiOpt = kanjiOriginRepository
                                .findByKanjiLessonIdAndTermAndDeletedFalse(lesson.getId(), kanjiRequest.getTerm());
                        
                        if (existingKanjiOpt.isEmpty()) {
                            // Create new kanji
                            Integer maxKanjiIndex = kanjiOriginRepository.findMaxContentIndexByKanjiLessonId(lesson.getId());
                            int newKanjiIndex = (maxKanjiIndex == null) ? 1 : maxKanjiIndex + 1;
                            
                            KanjiOrigin kanji = new KanjiOrigin();
                            kanji.setKanjiLesson(lesson);
                            kanji.setTerm(kanjiRequest.getTerm());
                            kanji.setPinyin(kanjiRequest.getPinyin());
                            kanji.setSinoVn(kanjiRequest.getSinoVn());
                            kanji.setMeaning(kanjiRequest.getMeaning());
                            kanji.setOriginImage(kanjiRequest.getOriginImage());
                            kanji.setOriginTextVi(kanjiRequest.getOriginTextVi());
                            kanji.setOriginTextCn(kanjiRequest.getOriginTextCn());
                            kanji.setExampleSentence(kanjiRequest.getExampleSentence());
                            kanji.setExampleMeaning(kanjiRequest.getExampleMeaning());
                            kanji.setExamplePinyin(kanjiRequest.getExamplePinyin());
                            kanji.setContentIndex(newKanjiIndex);
                            kanji.setDeleted(false);
                            kanjiOriginRepository.save(kanji);
                            kanjisCreated++;
                        }
                        // If kanji exists, skip (don't update)
                    } catch (Exception e) {
                        log.error("Error processing kanji: {}", kanjiRequest.getTerm(), e);
                        response.getErrors().add("Kanji '" + kanjiRequest.getTerm() + "': " + e.getMessage());
                    }
                }
                
                // Process questions
                for (ImportQuestionRequest questionRequest : lessonRequest.getQuestions()) {
                    try {
                        // Check if question already exists globally
                        Optional<KanjiQuestion> existingQuestionOpt = kanjiQuestionRepository
                                .findByContentAndCorrectAnswerAndDeletedFalse(
                                        questionRequest.getContent(),
                                        questionRequest.getCorrectAnswer());
                        
                        KanjiQuestion question;
                        if (existingQuestionOpt.isPresent()) {
                            question = existingQuestionOpt.get();
                            questionsReused++;
                        } else {
                            // Create new question
                            question = new KanjiQuestion();
                            question.setContent(questionRequest.getContent());
                            question.setCorrectAnswer(questionRequest.getCorrectAnswer());
                            question.setDeleted(false);
                            question = kanjiQuestionRepository.save(question);
                            
                            // Create wrong options
                            if (questionRequest.getWrongOptions() != null) {
                                for (String wrongOption : questionRequest.getWrongOptions()) {
                                    if (wrongOption != null && !wrongOption.trim().isEmpty()) {
                                        KanjiQuestionWrongOption option = new KanjiQuestionWrongOption();
                                        option.setKanjiQuestion(question);
                                        option.setWrongOption(wrongOption);
                                        option.setDeleted(false);
                                        kanjiQuestionWrongOptionRepository.save(option);
                                    }
                                }
                            }
                            questionsCreated++;
                        }
                        
                        // Link question to lesson if not already linked
                        boolean alreadyLinked = kanjiLessonQuestionRepository
                                .existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(lesson.getId(), question.getId());
                        
                        if (!alreadyLinked) {
                            Integer maxQuestionIndex = kanjiLessonQuestionRepository.findMaxContentIndexByKanjiLessonId(lesson.getId());
                            int newQuestionIndex = (maxQuestionIndex == null) ? 1 : maxQuestionIndex + 1;
                            
                            KanjiLessonQuestion lessonQuestion = new KanjiLessonQuestion();
                            lessonQuestion.setKanjiLesson(lesson);
                            lessonQuestion.setKanjiQuestion(question);
                            lessonQuestion.setContentIndex(newQuestionIndex);
                            lessonQuestion.setDeleted(false);
                            kanjiLessonQuestionRepository.save(lessonQuestion);
                        }
                    } catch (Exception e) {
                        log.error("Error processing question: {}", questionRequest.getContent(), e);
                        response.getErrors().add("Question '" + questionRequest.getContent() + "': " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing lesson: {}", lessonRequest.getTitle(), e);
                response.getErrors().add("Lesson '" + lessonRequest.getTitle() + "': " + e.getMessage());
            }
        }
        
        response.setLessonsCreated(lessonsCreated);
        response.setLessonsUpdated(lessonsUpdated);
        response.setKanjisCreated(kanjisCreated);
        response.setQuestionsCreated(questionsCreated);
        response.setQuestionsReused(questionsReused);
        response.setSuccess(response.getErrors().isEmpty());
        
        log.info("Import completed: {} lessons created, {} updated, {} kanjis, {} questions created, {} reused",
                lessonsCreated, lessonsUpdated, kanjisCreated, questionsCreated, questionsReused);
        
        return response;
    }

    @Override
    public KanjiLessonResponse createLesson(CreateKanjiLessonRequest request) {
        log.info("Creating kanji lesson with title: {}", request.getTitle());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found with id: " + request.getStudySetId()));

        if (request.getContentIndex() != null) {
            boolean exists = kanjiLessonRepository.existsByStudySetIdAndContentIndex(request.getStudySetId(), request.getContentIndex());
            if (exists) {
                throw new ApiException(ErrorCode.E227, "Content index " + request.getContentIndex() + " already exists in this StudySet");
            }
        }

        if (kanjiLessonRepository.existsByStudySetIdAndTitle(request.getStudySetId(), request.getTitle())) {
            throw new ApiException(ErrorCode.E227, "Title '" + request.getTitle() + "' already exists in this StudySet");
        }

        KanjiLesson lesson = kanjiLessonMapper.toEntity(request);
        lesson.setStudySet(studySet);
        KanjiLesson savedLesson = kanjiLessonRepository.save(lesson);
        return kanjiLessonMapper.toResponse(savedLesson);
    }

    @Override
    public KanjiLessonResponse updateLesson(String id, UpdateKanjiLessonRequest request) {
        log.info("Updating kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        kanjiLessonMapper.updateEntity(lesson, request);
        
        if (request.getContentIndex() != null) {
             boolean exists = kanjiLessonRepository.existsByStudySetIdAndContentIndexAndIdNot(
                 lesson.getStudySet().getId(), 
                 request.getContentIndex(), 
                 id
             );
             if (exists) {
                 throw new ApiException(ErrorCode.E227, "Content index " + request.getContentIndex() + " already exists in this StudySet");
             }
        }

        if (request.getTitle() != null && !request.getTitle().equals(lesson.getTitle())) {
             if (kanjiLessonRepository.existsByStudySetIdAndTitleAndIdNot(
                 lesson.getStudySet().getId(), 
                 request.getTitle(), 
                 id
             )) {
                 throw new ApiException(ErrorCode.E227, "Title '" + request.getTitle() + "' already exists in this StudySet");
             }
        }
        KanjiLesson savedLesson = kanjiLessonRepository.save(lesson);
        return kanjiLessonMapper.toResponse(savedLesson);
    }

    @Override
    public void deleteLesson(String id) {
        log.info("Deleting kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        if (!lesson.getKanjiOrigins().isEmpty()) {
            throw new ApiException(ErrorCode.E227, "Cannot delete lesson because it has kanji origins. Delete origins first.");
        }

        lesson.setDeleted(true);
        kanjiLessonRepository.save(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiLessonResponse getLesson(String id) {
        log.info("Getting kanji lesson with id: {}", id);

        KanjiLesson lesson = kanjiLessonRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + id));

        return kanjiLessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> getAllLessons() {
        log.info("Getting all kanji lessons");
        List<KanjiLesson> lessons = kanjiLessonRepository.findByDeletedFalse();
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> getLessonsByStudySetId(String studySetId) {
        log.info("Getting kanji lessons by study set id: {}", studySetId);
        List<KanjiLesson> lessons = kanjiLessonRepository.findByStudySetIdAndDeletedFalse(studySetId);
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiLessonBasicResponse> search(KanjiLessonSearchRequest request) {
        log.info("Searching kanji lessons with studySetId: {}, keyword: {}", request.getStudySetId(), request.getKeyword());
        List<KanjiLesson> lessons = kanjiLessonRepository.searchList(request.getStudySetId(), request.getKeyword());
        return kanjiLessonMapper.toBasicResponseList(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KanjiLessonBasicResponse> searchPaged(KanjiLessonSearchRequest request) {
        log.info("Searching paged kanji lessons with studySetId: {}, keyword: {}", request.getStudySetId(), request.getKeyword());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<KanjiLesson> page = kanjiLessonRepository.search(request.getStudySetId(), request.getKeyword(), pageable);
        
        List<KanjiLessonBasicResponse> items = kanjiLessonMapper.toBasicResponseList(page.getContent());
        
        return PageResponse.<KanjiLessonBasicResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }
}

