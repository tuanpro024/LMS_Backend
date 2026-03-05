package com.lms.videocourse.service.impl;

import com.lms.videocourse.dto.request.CreateVideoCourseRequest;
import com.lms.videocourse.dto.request.UpdateVideoCourseRequest;
import com.lms.videocourse.dto.response.VideoCourseResponse;
import com.lms.videocourse.entity.VideoCourse;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoCourseRepository;
import com.lms.videocourse.service.IVideoCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCourseServiceImpl implements IVideoCourseService {

    private final VideoCourseRepository videoCourseRepository;

    @Override
    @Transactional
    public VideoCourseResponse createVideoCourse(CreateVideoCourseRequest request, String createdBy) {
        log.info("Creating video course: {} by {}", request.getTitle(), createdBy);

        VideoCourse course = VideoCourse.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .estimatedHours(request.getEstimatedHours())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        // Set studySetId from BaseContentItem
        course.setStudySetId(request.getStudySetId());
        if (request.getContentIndex() != null) {
            course.setContentIndex(request.getContentIndex());
        }

        course = videoCourseRepository.save(course);
        return toResponse(course);
    }

    @Override
    public VideoCourseResponse getVideoCourseById(String id) {
        VideoCourse course = videoCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video course not found: " + id));
        return toResponse(course);
    }

    @Override
    public List<VideoCourseResponse> getVideoCoursesByStudySetId(String studySetId) {
        return videoCourseRepository
                .findByStudySetIdAndIsActiveTrueOrderByCreatedAtAsc(studySetId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoCourseResponse updateVideoCourse(String id, UpdateVideoCourseRequest request, String updatedBy) {
        VideoCourse course = videoCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video course not found: " + id));

        if (request.getTitle() != null)
            course.setTitle(request.getTitle());
        if (request.getDescription() != null)
            course.setDescription(request.getDescription());
        if (request.getThumbnail() != null)
            course.setThumbnail(request.getThumbnail());
        if (request.getEstimatedHours() != null)
            course.setEstimatedHours(request.getEstimatedHours());
        if (request.getIsActive() != null)
            course.setIsActive(request.getIsActive());
        if (request.getContentIndex() != null)
            course.setContentIndex(request.getContentIndex());

        course = videoCourseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public void deleteVideoCourse(String id) {
        VideoCourse course = videoCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video course not found: " + id));
        course.setIsActive(false); // soft delete
        videoCourseRepository.save(course);
    }

    private VideoCourseResponse toResponse(VideoCourse course) {
        return VideoCourseResponse.builder()
                .id(course.getId())
                .studySetId(course.getStudySetId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnail(course.getThumbnail())
                .estimatedHours(course.getEstimatedHours())
                .isActive(course.getIsActive())
                .createdBy(course.getCreatedBy())
                .contentIndex(course.getContentIndex())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
