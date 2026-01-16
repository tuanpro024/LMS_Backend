package com.lms.dictionary.service;

import com.lms.common.dto.PageResponse;
import com.lms.common.dto.PaginationRequest;

import com.lms.dictionary.dto.request.CreateVocabularyRequest;

import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.request.VocabularySearchRequest;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;

public interface VocabularyService {

    VocabularyResponse getVocabularyById(Long id);
    VocabularyResponse createVocabulary(CreateVocabularyRequest request);
    VocabularyResponse updateVocabulary(Long id, UpdateVocabularyRequest request);

    void deleteVocabulary(Long id);

    PageResponse<VocabularyBasicResponse> search(VocabularySearchRequest request);

    PageResponse<VocabularyBasicResponse> getSuggestions(PaginationRequest request);


}
