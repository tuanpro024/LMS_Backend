package com.lms.dictionary.mapper;

import com.lms.dictionary.dto.request.UpdateVocabularyMeaningRequest;
import com.lms.dictionary.dto.request.VocabularyMeaningRequest;

import com.lms.dictionary.dto.response.VocabularyMeaningResponse;
import com.lms.dictionary.entity.VocabularyMeaning;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface VocabularyMeaningMapper {

    @Mapping(target = "vocabulary", ignore = true)
    VocabularyMeaning toEntity(VocabularyMeaningRequest request);

    @Mapping(target = "vocabulary", ignore = true)
    VocabularyMeaning toEntity(UpdateVocabularyMeaningRequest request);

    List<VocabularyMeaning> toEntityList(List<VocabularyMeaningRequest> requests);

    List<VocabularyMeaning> toUpdateEntityList(List<UpdateVocabularyMeaningRequest> requests);

    VocabularyMeaningResponse toResponse(VocabularyMeaning meaning);

    List<VocabularyMeaningResponse> toResponseList(List<VocabularyMeaning> meanings);
}
