package com.lms.kanjiorigin.service;

import com.lms.common.dto.PageResponse;
import com.lms.kanjiorigin.dto.request.CreateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.request.KanjiLessonSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiLessonRequest;
import com.lms.kanjiorigin.dto.response.ImportResultResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonBasicResponse;
import com.lms.kanjiorigin.dto.response.KanjiLessonResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KanjiLessonService {
    KanjiLessonResponse createLesson(CreateKanjiLessonRequest request);
    KanjiLessonResponse updateLesson(String id, UpdateKanjiLessonRequest request);
    void deleteLesson(String id);
    KanjiLessonResponse getLesson(String id);
    List<KanjiLessonBasicResponse> getAllLessons();
    List<KanjiLessonBasicResponse> getLessonsByStudySetId(String studySetId);
    List<KanjiLessonBasicResponse> search(KanjiLessonSearchRequest request);
    PageResponse<KanjiLessonBasicResponse> searchPaged(KanjiLessonSearchRequest request);
    ImportResultResponse importFromExcel(String studySetId, MultipartFile file);
}
