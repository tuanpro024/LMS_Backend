package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.mapper.KanjiOriginMapper;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import com.lms.kanjiorigin.service.KanjiOriginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiOriginServiceImpl implements KanjiOriginService {

    private final KanjiOriginRepository kanjiOriginRepository;
    private final KanjiLessonRepository kanjiLessonRepository;
    private final KanjiOriginMapper kanjiOriginMapper;

    @Override
    public KanjiOriginResponse createOrigin(CreateKanjiOriginRequest request) {
        log.info("Creating kanji origin with term: {}", request.getTerm());

        KanjiLesson lesson = kanjiLessonRepository.findById(request.getKanjiLessonId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + request.getKanjiLessonId()));

        if (kanjiOriginRepository.existsByTermAndKanjiLessonIdAndDeletedFalse(request.getTerm(), request.getKanjiLessonId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this lesson: " + request.getTerm());
        }

        KanjiOrigin origin = kanjiOriginMapper.toEntity(request);
        origin.setKanjiLesson(lesson);

        KanjiOrigin savedOrigin = kanjiOriginRepository.save(origin);
        return kanjiOriginMapper.toResponse(savedOrigin);
    }

    @Override
    public KanjiOriginResponse updateOrigin(String id, UpdateKanjiOriginRequest request) {
        log.info("Updating kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + id));

        if (!origin.getTerm().equals(request.getTerm()) &&
            kanjiOriginRepository.existsByTermAndKanjiLessonIdAndDeletedFalse(request.getTerm(), origin.getKanjiLesson().getId())) {
            throw new ApiException(ErrorCode.E227, "Term already exists in this lesson: " + request.getTerm());
        }

        kanjiOriginMapper.updateEntity(origin, request);
        KanjiOrigin savedOrigin = kanjiOriginRepository.save(origin);
        return kanjiOriginMapper.toResponse(savedOrigin);
    }

    @Override
    public void deleteOrigin(String id) {
        log.info("Deleting kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + id));

        origin.setDeleted(true);
        kanjiOriginRepository.save(origin);
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiOriginResponse getOrigin(String id) {
        log.info("Getting kanji origin with id: {}", id);

        KanjiOrigin origin = kanjiOriginRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + id));

        return kanjiOriginMapper.toResponse(origin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiOriginResponse> getOriginsByLesson(String lessonId) {
        log.info("Getting kanji origins for lesson: {}", lessonId);

        if (!kanjiLessonRepository.existsById(lessonId)) {
            throw new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId);
        }

        List<KanjiOrigin> origins = kanjiOriginRepository.findByKanjiLessonIdAndDeletedFalse(lessonId);
        return kanjiOriginMapper.toResponseList(origins);
    }
}
