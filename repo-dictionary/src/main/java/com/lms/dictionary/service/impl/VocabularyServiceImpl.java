package com.lms.dictionary.service.impl;

import com.lms.common.dto.PageResponse;
import com.lms.common.dto.PaginationRequest;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.UpdateVocabComponentRequest;
import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.request.VocabularySearchRequest;
import com.lms.dictionary.dto.request.VocabComponentRequest;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;
import com.lms.dictionary.entity.VocabComponent;
import com.lms.dictionary.entity.Vocabulary;
import com.lms.dictionary.entity.VocabularyMeaning;
import com.lms.dictionary.mapper.VocabularyMapper;
import com.lms.dictionary.mapper.VocabularyMeaningMapper;
import com.lms.dictionary.repository.VocabularyRepository;
import com.lms.dictionary.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VocabularyServiceImpl implements VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyMapper vocabularyMapper;
    private final VocabularyMeaningMapper vocabularyMeaningMapper;

    @Override
    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyById(Long id) {
        if (id == null) {
            throw new ApiException(ErrorCode.E227, "Invalid ID");
        }
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Vocabulary not found"));
        return vocabularyMapper.toResponse(vocabulary);
    }

    @Override
    @Transactional
    public VocabularyResponse createVocabulary(CreateVocabularyRequest request) {
        if (vocabularyRepository.existsByHanziAndDeletedFalse(request.getHanzi())) {
            throw new ApiException(ErrorCode.E227, "Hanzi already exists: " + request.getHanzi());
        }

        Vocabulary vocabulary = processCreateVocabulary(request);
        Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
        return vocabularyMapper.toResponse(savedVocabulary);
    }

    private Vocabulary processCreateVocabulary(CreateVocabularyRequest request) {
        Vocabulary vocabulary = vocabularyMapper.toEntity(request);

        if (request.getMeanings() != null) {
            List<VocabularyMeaning> meanings = vocabularyMeaningMapper.toEntityList(request.getMeanings());
            meanings.forEach(m -> m.setVocabulary(vocabulary));
            vocabulary.setMeanings(meanings);
        }

        if (request.getComponents() != null) {
            List<VocabComponent> components = new ArrayList<>();
            for (VocabComponentRequest cReq : request.getComponents()) {
                Vocabulary componentVocab;
                if (cReq.getId() != null) {
                    componentVocab = vocabularyRepository.findById(cReq.getId())
                            .orElseThrow(() -> new ApiException(ErrorCode.E227, "Component vocabulary not found with id: " + cReq.getId()));
                } else if (cReq.getNewVocabulary() != null) {
                    componentVocab = vocabularyRepository.findByHanziAndPinyinAndDeletedFalse(
                            cReq.getNewVocabulary().getHanzi(),
                            cReq.getNewVocabulary().getPinyin())
                            .orElse(null);
                    if (componentVocab == null) {
                        componentVocab = processCreateVocabulary(cReq.getNewVocabulary());
                        componentVocab = vocabularyRepository.save(componentVocab);
                    }
                } else {
                    throw new ApiException(ErrorCode.E227, "Each component must have either an id or a newVocabulary object");
                }

                VocabComponent component = VocabComponent.builder()
                        .parentVocab(vocabulary)
                        .componentVocab(componentVocab)
                        .orderIndex(cReq.getOrderIndex())
                        .build();
                components.add(component);
            }
            vocabulary.setSubVocabs(components);
        }

        if (request.getComponentIds() != null) {
            for (int i = 0; i < request.getComponentIds().size(); i++) {
                Long componentId = request.getComponentIds().get(i);
                Vocabulary componentVocab = vocabularyRepository.findById(componentId)
                        .orElseThrow(() -> new ApiException(ErrorCode.E227, "Component vocabulary not found with id: " + componentId));
                
                if (!componentVocab.getIsSingleVocab()) {
                    throw new ApiException(ErrorCode.E227, "Only single vocabularies can be components: " + componentVocab.getHanzi());
                }

                VocabComponent component = VocabComponent.builder()
                        .parentVocab(vocabulary)
                        .componentVocab(componentVocab)
                        .orderIndex(i + 1)
                        .build();
                vocabulary.getSubVocabs().add(component);

            }
        }
        return vocabulary;
    }

    @Override
    @Transactional
    public VocabularyResponse updateVocabulary(Long id, UpdateVocabularyRequest request) {
        if (id == null) {
            throw new ApiException(ErrorCode.E227, "Invalid ID");
        }
        log.info("Updating vocabulary with id: {}", id);
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Vocabulary not found"));

        if (!vocabulary.getHanzi().equals(request.getHanzi()) && 
            vocabularyRepository.existsByHanziAndDeletedFalse(request.getHanzi())) {
            throw new ApiException(ErrorCode.E227, "Hanzi already exists: " + request.getHanzi());
        }

        vocabularyMapper.partialUpdate(vocabulary, request);

        vocabulary.getMeanings().clear();
        if (request.getMeanings() != null) {
            List<VocabularyMeaning> newMeanings = vocabularyMeaningMapper.toUpdateEntityList(request.getMeanings());
            newMeanings.forEach(m -> {
                m.setVocabulary(vocabulary);
                vocabulary.getMeanings().add(m);
            });
        }

        vocabulary.getSubVocabs().clear();
        if (request.getComponents() != null) {
            for (UpdateVocabComponentRequest cReq : request.getComponents()) {
                Vocabulary componentVocab;
                if (cReq.getId() != null) {
                    componentVocab = vocabularyRepository.findById(cReq.getId())
                            .orElseThrow(() -> new ApiException(ErrorCode.E227, "Component vocabulary not found with id: " + cReq.getId()));
                } else if (cReq.getNewVocabulary() != null) {
                    // Smart Lookup: Tự động dùng từ đã có nếu trùng Hanzi & Pinyin
                    componentVocab = vocabularyRepository.findByHanziAndPinyinAndDeletedFalse(
                            cReq.getNewVocabulary().getHanzi(),
                            cReq.getNewVocabulary().getPinyin())
                            .orElse(null);
                    if (componentVocab == null) {
                        componentVocab = processCreateVocabulary(cReq.getNewVocabulary());
                        componentVocab = vocabularyRepository.save(componentVocab);
                    }
                } else {
                    throw new ApiException(ErrorCode.E227, "Each component must have either an id or a newVocabulary object");
                }

                if (Boolean.FALSE.equals(componentVocab.getIsSingleVocab())) {
                    throw new ApiException(ErrorCode.E227, "Only single vocabularies can be components: " + componentVocab.getHanzi());
                }

                VocabComponent component = VocabComponent.builder()
                        .parentVocab(vocabulary)
                        .componentVocab(componentVocab)
                        .orderIndex(cReq.getOrderIndex())
                        .build();
                vocabulary.getSubVocabs().add(component);
            }
        }

        if (request.getComponentIds() != null) {
            for (int i = 0; i < request.getComponentIds().size(); i++) {
                Long componentId = request.getComponentIds().get(i);
                Vocabulary componentVocab = vocabularyRepository.findById(componentId)
                        .orElseThrow(() -> new ApiException(ErrorCode.E227, "Component vocabulary not found with id: " + componentId));
                
                if (Boolean.FALSE.equals(componentVocab.getIsSingleVocab())) {
                    throw new ApiException(ErrorCode.E227, "Only single vocabularies can be components: " + componentVocab.getHanzi());
                }

                VocabComponent component = VocabComponent.builder()
                        .parentVocab(vocabulary)
                        .componentVocab(componentVocab)
                        .orderIndex(i + 1)
                        .build();
                vocabulary.getSubVocabs().add(component);
            }
        }


        Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
        return vocabularyMapper.toResponse(savedVocabulary);
    }

    @Override
    @Transactional
    public void deleteVocabulary(Long id) {
        log.info("Deleting vocabulary with id: {}", id);
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Vocabulary not found"));

        if (!vocabulary.getParentVocabs().isEmpty()) {
            throw new ApiException(ErrorCode.E227, "Cannot delete vocabulary because it is used as a component in other vocabularies");
        }

        vocabularyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyBasicResponse> search(VocabularySearchRequest request) {
        log.info("Searching vocabulary with request: {}", request);
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Vocabulary> vocabPage = vocabularyRepository.searchByQuery(
                request.getKeySearch(), request.getIsSingleVocab(), pageable);
        
        return PageResponse.<VocabularyBasicResponse>builder()
                .items(vocabularyMapper.toBasicResponseList(vocabPage.getContent()))
                .totalElements(vocabPage.getTotalElements())
                .totalPages(vocabPage.getTotalPages())
                .page(vocabPage.getNumber())
                .size(vocabPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VocabularyBasicResponse> getSuggestions(PaginationRequest request) {
        log.info("Getting vocabulary suggestions with request: {}", request);
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Vocabulary> vocabPage = vocabularyRepository.findSuggestions(pageable);
        
        return PageResponse.<VocabularyBasicResponse>builder()
                .items(vocabularyMapper.toBasicResponseList(vocabPage.getContent()))
                .totalElements(vocabPage.getTotalElements())
                .totalPages(vocabPage.getTotalPages())
                .page(vocabPage.getNumber())
                .size(vocabPage.getSize())
                .build();
    }


}
