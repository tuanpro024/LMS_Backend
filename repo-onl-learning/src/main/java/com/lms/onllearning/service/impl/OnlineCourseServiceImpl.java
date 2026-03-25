package com.lms.onllearning.service.impl;

import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.request.OnlineCourseRequest;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.OnlineCourseFullDetailResponse;
import com.lms.onllearning.dto.response.OnlineCourseResponse;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.entity.OnlineCourse;
import com.lms.onllearning.repository.OnlineCourseRepository;
import com.lms.onllearning.service.IOnlineCourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineCourseServiceImpl implements IOnlineCourseService {

    private final OnlineCourseRepository courseRepo;
    private final CmsClient cmsClient;

    @Override
    public List<OnlineCourseResponse> getAll() {
        return courseRepo.findAllByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OnlineCourseResponse getById(String id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public OnlineCourseFullDetailResponse getFullDetail(String id) {
        OnlineCourse entity = findOrThrow(id);
        CmsEnvelope<SyllabusDetailResponse> syllabus = entity.getSyllabusId() == null
                || entity.getSyllabusId().isBlank()
                        ? CmsEnvelope.notFound()
                        : cmsClient.getSyllabusDetail(entity.getSyllabusId());
        return new OnlineCourseFullDetailResponse(toResponse(entity), syllabus);
    }

    @Override
    @Transactional
    public OnlineCourseResponse create(OnlineCourseRequest request) {
        OnlineCourse entity = OnlineCourse.builder()
                .code(request.code())
                .name(request.name())
                .courseType(request.courseType())
                .level(request.level())
                .totalLessons(request.totalLessons())
                .thumbnail(request.thumbnail())
                .description(request.description())
                .syllabusId(request.syllabusId())
                .price(request.price())
                .rating(request.rating())
                .cmsSynced(false)
                .build();
        return toResponse(courseRepo.save(entity));
    }

    @Override
    @Transactional
    public OnlineCourseResponse update(String id, OnlineCourseRequest request) {
        OnlineCourse entity = findOrThrow(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setCourseType(request.courseType());
        entity.setLevel(request.level());
        entity.setTotalLessons(request.totalLessons());
        entity.setThumbnail(request.thumbnail());
        entity.setDescription(request.description());
        entity.setSyllabusId(request.syllabusId());
        entity.setPrice(request.price());
        if (request.rating() != null)
            entity.setRating(request.rating());
        return toResponse(courseRepo.save(entity));
    }

    @Override
    @Transactional
    public void delete(String id) {
        OnlineCourse entity = findOrThrow(id);
        entity.setDeleted(true);
        courseRepo.save(entity);
    }

    // -----------------------------------------------------------------------

    private OnlineCourse findOrThrow(String id) {
        return courseRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học: " + id));
    }

    private OnlineCourseResponse toResponse(OnlineCourse e) {
        return new OnlineCourseResponse(
                e.getId(), e.getCode(), e.getName(), e.getCourseType(), e.getLevel(), e.getTotalLessons(),
                e.getThumbnail(), e.getDescription(),
                e.getSyllabusId(), e.getPrice(), e.getRating(), e.isCmsSynced(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
