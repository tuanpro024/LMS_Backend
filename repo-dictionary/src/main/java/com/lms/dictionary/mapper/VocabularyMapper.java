package com.lms.dictionary.mapper;

import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;
import com.lms.dictionary.entity.Vocabulary;
import com.lms.dictionary.enums.WordType;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {VocabularyMeaningMapper.class}, builder = @Builder(disableBuilder = true))
public interface VocabularyMapper {

    @Mapping(target = "wordTypes", expression = "java(mapStringListToEnumList(request.getWordTypes()))")
    Vocabulary toEntity(CreateVocabularyRequest request);

    @Mapping(target = "wordTypes", expression = "java(mapStringListToEnumList(request.getWordTypes()))")
    void partialUpdate(@MappingTarget Vocabulary vocabulary, UpdateVocabularyRequest request);

    @Mapping(target = "componentVocabs", expression = "java(toResponseList(vocabulary.getSubVocabs().stream().map(c -> c.getComponentVocab()).toList()))")
    @Mapping(target = "wordTypes", expression = "java(mapEnumListToStringList(vocabulary.getWordTypes()))")
    VocabularyResponse toResponse(Vocabulary vocabulary);

    List<VocabularyResponse> toResponseList(List<Vocabulary> vocabularies);

    @Mapping(target = "meanings", expression = "java(vocabulary.getMeanings().stream().map(m -> m.getMeaning()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "wordTypes", expression = "java(mapEnumListToStringList(vocabulary.getWordTypes()))")
    VocabularyBasicResponse toBasicResponse(Vocabulary vocabulary);

    List<VocabularyBasicResponse> toBasicResponseList(List<Vocabulary> vocabularies);
    
    default List<WordType> mapStringListToEnumList(List<String> types) {
        if (types == null) return new ArrayList<>();
        return types.stream()
                .map(t -> {
                    try {
                        return WordType.fromValue(t);
                    } catch (Exception e) {
                        try {
                           return WordType.valueOf(t.toUpperCase());
                        } catch(Exception ex) {
                           return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default List<String> mapEnumListToStringList(List<WordType> types) {
        if (types == null) return new ArrayList<>();
        return types.stream().map(WordType::getValue).collect(Collectors.toList());
    }
}
