package com.lms.aipractice.service.impl;

import com.lms.aipractice.adapter.MultimediaFileClient;
import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;
import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import com.lms.aipractice.mapper.AiPracticeMapper;
import com.lms.aipractice.repository.AiPracticeItemRepository;
import com.lms.aipractice.service.AiPracticeItemService;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiPracticeItemServiceImpl implements AiPracticeItemService {

    private final AiPracticeItemRepository itemRepository;
    private final StudySetRepository studySetRepository;
    private final AiPracticeMapper mapper;
    private final MultimediaFileClient multimediaFileClient;

    @Override
    public AiPracticeItemResponse create(CreateAiPracticeItemRequest request) {
        return createInternal(request, null, null);
    }

    @Override
    public AiPracticeItemResponse createWithAudio(CreateAiPracticeItemRequest request, MultipartFile audioFile) {
        return createInternal(request, audioFile, null);
    }

    @Override
    public AiPracticeItemResponse createWithImage(CreateAiPracticeItemRequest request, MultipartFile imageFile) {
        return createInternal(request, null, imageFile);
    }

    private AiPracticeItemResponse createInternal(
            CreateAiPracticeItemRequest request,
            MultipartFile audioFile,
            MultipartFile imageFile) {
        log.info("Creating AI practice item subtype={} in studySet={}", request.getQuestionSubtype(),
                request.getStudySetId());

        boolean hasAudioUpload = audioFile != null && !audioFile.isEmpty();
        boolean hasImageUpload = imageFile != null && !imageFile.isEmpty();
        boolean isListenAndAnswer = request.getQuestionSubtype() == AiItemSubtype.SPEAKING_LISTEN_AND_ANSWER;
        boolean isWritingPicture = request.getQuestionSubtype() == AiItemSubtype.PICTURE_SENTENCE
                || request.getQuestionSubtype() == AiItemSubtype.PICTURE_PARAGRAPH;

        if (hasAudioUpload && !isListenAndAnswer) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "Audio upload is only supported for SPEAKING_LISTEN_AND_ANSWER");
        }

        if (hasImageUpload && !isWritingPicture) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "Image upload is only supported for PICTURE_SENTENCE and PICTURE_PARAGRAPH");
        }

        if (hasAudioUpload) {
            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "Uploaded file must be an audio file");
            }
            String fileId = multimediaFileClient.upload(audioFile);
            request.setQuestionAudioFileId(fileId);
        }

        if (hasImageUpload) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "Uploaded file must be an image file");
            }
            String fileId = multimediaFileClient.upload(imageFile);
            request.setQuestionImageFileId(fileId);
        }

        if (isListenAndAnswer
                && (request.getQuestionAudioFileId() == null || request.getQuestionAudioFileId().isBlank())) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "questionAudioFileId is required for SPEAKING_LISTEN_AND_ANSWER");
        }

        if (isWritingPicture
                && (request.getQuestionImageFileId() == null || request.getQuestionImageFileId().isBlank())) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "questionImageFileId is required for PICTURE_SENTENCE and PICTURE_PARAGRAPH");
        }

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found: " + request.getStudySetId()));

        if (request.getContentIndex() != null &&
                itemRepository.existsByStudySetIdAndContentIndexAndDeletedFalse(
                        request.getStudySetId(), request.getContentIndex())) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        AiPracticeItem item = mapper.toEntity(request);
        item.setStudySet(studySet);

        // Auto-assign contentIndex
        if (request.getContentIndex() == null) {
            Integer maxIndex = itemRepository.findMaxContentIndexByStudySetId(request.getStudySetId());
            item.setContentIndex(maxIndex == null ? 1 : maxIndex + 1);
        } else {
            item.setContentIndex(request.getContentIndex());
        }

        return withMediaUrls(mapper.toResponse(itemRepository.save(item)));
    }

    @Override
    public AiPracticeItemResponse update(String id, UpdateAiPracticeItemRequest request) {
        log.info("Updating AI practice item id={}", id);

        AiPracticeItem item = itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "AiPracticeItem not found: " + id));

        if (request.getContentIndex() != null &&
                !request.getContentIndex().equals(item.getContentIndex()) &&
                itemRepository.existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(
                        item.getStudySet().getId(), request.getContentIndex(), id)) {
            throw new ApiException(ErrorCode.E227, "Content index already exists: " + request.getContentIndex());
        }

        mapper.updateEntity(item, request);
        return withMediaUrls(mapper.toResponse(itemRepository.save(item)));
    }

    @Override
    public void delete(String id) {
        log.info("Deleting AI practice item id={}", id);
        AiPracticeItem item = itemRepository.findById(id).orElse(null);
        if (item == null || item.isDeleted()) {
            log.warn("Delete no-op for item id={}", id);
            return;
        }
        item.setDeleted(true);
        itemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public AiPracticeItemResponse getById(String id) {
        return withMediaUrls(mapper.toResponse(
                itemRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new ApiException(ErrorCode.E227, "AiPracticeItem not found: " + id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPracticeItemResponse> getByStudySetId(String studySetId) {
        if (!studySetRepository.existsById(studySetId)) {
            throw new ApiException(ErrorCode.E227, "StudySet not found: " + studySetId);
        }
        return mapper.toResponseList(
                itemRepository.findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(studySetId))
                .stream()
                .map(this::withMediaUrls)
                .collect(Collectors.toList());
    }

    private AiPracticeItemResponse withMediaUrls(AiPracticeItemResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getQuestionAudioFileId() != null && !response.getQuestionAudioFileId().isBlank()) {
            response.setQuestionAudioUrl(multimediaFileClient.buildFileAccessUrl(response.getQuestionAudioFileId()));
        }
        if (response.getQuestionImageFileId() != null && !response.getQuestionImageFileId().isBlank()) {
            response.setQuestionImageUrl(multimediaFileClient.buildFileAccessUrl(response.getQuestionImageFileId()));
        }
        return response;
    }
}
