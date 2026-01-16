package com.lms.dictionary.mapper;

import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;
import com.lms.dictionary.entity.Vocabulary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {VocabularyMeaningMapper.class})
public interface VocabularyMapper {

    Vocabulary toEntity(CreateVocabularyRequest request);

    void partialUpdate(@MappingTarget Vocabulary vocabulary, UpdateVocabularyRequest request);

    @Mapping(target = "componentVocabs", expression = "java(toResponseList(vocabulary.getSubVocabs().stream().map(c -> c.getComponentVocab()).toList()))")
    VocabularyResponse toResponse(Vocabulary vocabulary);

    List<VocabularyResponse> toResponseList(List<Vocabulary> vocabularies);

    @Mapping(target = "meanings", expression = "java(vocabulary.getMeanings().stream().map(m -> m.getMeaning()).collect(java.util.stream.Collectors.toList()))")
    VocabularyBasicResponse toBasicResponse(Vocabulary vocabulary);

    List<VocabularyBasicResponse> toBasicResponseList(List<Vocabulary> vocabularies);
}
