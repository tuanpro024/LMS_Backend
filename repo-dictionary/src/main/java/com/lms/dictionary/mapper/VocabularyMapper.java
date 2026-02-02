package com.lms.dictionary.mapper;

import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.UpdateVocabularyRequest;
import com.lms.dictionary.dto.response.VocabularyBasicResponse;
import com.lms.dictionary.dto.response.VocabularyResponse;
import com.lms.dictionary.dto.response.VocabularyMeaningResponse;
import com.lms.dictionary.entity.VocabComponent;
import com.lms.dictionary.entity.Vocabulary;
import com.lms.dictionary.entity.VocabularyMeaning;
import com.lms.dictionary.enums.WordType;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { VocabularyMeaningMapper.class }, builder = @Builder(disableBuilder = true))
public interface VocabularyMapper {

    @Mapping(target = "wordTypes", source = "wordTypes", qualifiedByName = "stringToEnum")
    @Mapping(target = "meanings", ignore = true)
    @Mapping(target = "subVocabs", ignore = true)
    @Mapping(target = "parentVocabs", ignore = true)
    Vocabulary toEntity(CreateVocabularyRequest request);

    @Mapping(target = "wordTypes", source = "wordTypes", qualifiedByName = "stringToEnum")
    @Mapping(target = "meanings", ignore = true)
    @Mapping(target = "subVocabs", ignore = true)
    @Mapping(target = "parentVocabs", ignore = true)
    void partialUpdate(@MappingTarget Vocabulary vocabulary, UpdateVocabularyRequest request);

    @Mapping(target = "componentVocabs", source = "subVocabs")
    @Mapping(target = "wordTypes", source = "wordTypes", qualifiedByName = "enumToString")
    @Mapping(target = "wordType", ignore = true)
    VocabularyResponse toResponse(Vocabulary vocabulary);

    @Mapping(target = "id", source = "componentVocab.id")
    @Mapping(target = "hskLevel", source = "componentVocab.hskLevel")
    @Mapping(target = "wordTypes", source = "componentVocab.wordTypes", qualifiedByName = "enumToString")
    @Mapping(target = "hanzi", source = "componentVocab.hanzi")
    @Mapping(target = "pinyin", source = "componentVocab.pinyin")
    @Mapping(target = "audioUrl", source = "componentVocab.audioUrl")
    @Mapping(target = "imageUrl", source = "componentVocab.imageUrl")
    @Mapping(target = "strokeAnimationUrl", source = "componentVocab.strokeAnimationUrl")
    @Mapping(target = "etymologyStory", source = "componentVocab.etymologyStory")
    @Mapping(target = "etymologyImage", source = "componentVocab.etymologyImage")
    @Mapping(target = "isSingleVocab", source = "componentVocab.isSingleVocab")
    @Mapping(target = "meanings", source = "componentVocab.meanings")
    @Mapping(target = "componentVocabs", ignore = true)
    @Mapping(target = "wordType", ignore = true)
    VocabularyResponse toResponse(VocabComponent component);

    List<VocabularyResponse> toResponseList(List<Vocabulary> vocabularies);

    @Mapping(target = "wordTypes", source = "wordTypes", qualifiedByName = "enumToString")
    @Mapping(target = "meanings", source = "meanings", qualifiedByName = "mapMeanings")
    VocabularyBasicResponse toBasicResponse(Vocabulary vocabulary);

    List<VocabularyBasicResponse> toBasicResponseList(List<Vocabulary> vocabularies);

    @Named("enumToString")
    default List<String> mapEnumListToStringList(List<WordType> types) {
        if (types == null)
            return new ArrayList<>();
        return types.stream().map(WordType::getValue).collect(Collectors.toList());
    }

    @Named("mapMeanings")
    default List<String> mapMeaningsToStringList(List<VocabularyMeaning> meanings) {
        if (meanings == null)
            return new ArrayList<>();
        return meanings.stream().map(VocabularyMeaning::getMeaning).collect(Collectors.toList());
    }

    @Named("stringToEnum")
    default List<WordType> mapStringListToEnumList(List<String> types) {
        if (types == null)
            return new ArrayList<>();
        return types.stream()
                .map(t -> {
                    try {
                        return WordType.fromValue(t);
                    } catch (Exception e) {
                        try {
                            return WordType.valueOf(t.toUpperCase());
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
