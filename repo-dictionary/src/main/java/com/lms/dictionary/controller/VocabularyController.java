package com.lms.dictionary.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.common.dto.PageResponse;
import com.lms.common.dto.PaginationRequest;
import com.lms.common.dto.ApiResponse;
import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.SyncEtymologyRequest;
import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.request.VocabularySearchRequest;
import com.lms.dictionary.dto.response.ImportResult;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;
import com.lms.dictionary.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/vocabularies")
@RequiredArgsConstructor

public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/{id}")
    public ApiResponse<VocabularyResponse> getVocabularyById(@PathVariable Long id) {
        return ApiResponse.ok(vocabularyService.getVocabularyById(id));
    }

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.DICTIONARY)
    public ApiResponse<VocabularyResponse> createVocabulary(@Valid @RequestBody CreateVocabularyRequest request) {
        return ApiResponse.ok(vocabularyService.createVocabulary(request));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.DICTIONARY)
    public ApiResponse<VocabularyResponse> updateVocabulary(@PathVariable Long id, @RequestBody @Valid UpdateVocabularyRequest request) {
        return ApiResponse.ok(vocabularyService.updateVocabulary(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.DICTIONARY)
    public ApiResponse<Void> deleteVocabulary(@PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<VocabularyBasicResponse>> search(@Valid VocabularySearchRequest request) {
        return ApiResponse.ok(vocabularyService.search(request));
    }

    @GetMapping("/suggestions")
    public ApiResponse<PageResponse<VocabularyBasicResponse>> getSuggestions(@Valid PaginationRequest request) {
        return ApiResponse.ok(vocabularyService.getSuggestions(request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresTicket(module = TicketModuleEnum.DICTIONARY)
    public ApiResponse<ImportResult> importVocabularies(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(vocabularyService.importVocabularies(file));
    }

    @PatchMapping("/{id}/sync-etymology")
    public ApiResponse<Void> syncEtymologyImage(@PathVariable Long id, @Valid @RequestBody SyncEtymologyRequest request) {
        vocabularyService.syncEtymologyImage(id, request);
        return ApiResponse.ok(null);
    }

}
