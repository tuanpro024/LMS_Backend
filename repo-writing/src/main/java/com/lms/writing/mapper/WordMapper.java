package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreateWordRequest;
import com.lms.writing.dto.request.UpdateWordRequest;
import com.lms.writing.dto.response.WordResponse;
import com.lms.writing.entity.Word;
import com.lms.writing.util.CharacterUtils;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WordMapper {

    @Mapping(target = "studySet", ignore = true)
    @Mapping(target = "characters", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Word toEntity(CreateWordRequest request);

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "characters", ignore = true)
    WordResponse toResponse(Word word);

    List<WordResponse> toResponseList(List<Word> words);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "studySet", ignore = true)
    @Mapping(target = "characters", ignore = true)
    void updateEntity(@MappingTarget Word word, UpdateWordRequest request);

    // Generate characters JSON from word after mapping (for create)
    @AfterMapping
    default void generateCharactersAfterCreate(@MappingTarget Word word, CreateWordRequest request) {
        if (request.getWord() != null && !request.getWord().isEmpty()) {
            word.setCharacters(CharacterUtils.wordToJsonArray(request.getWord()));
        }
    }

    // Generate characters JSON from word after mapping (for update)
    @AfterMapping
    default void generateCharactersAfterUpdate(@MappingTarget Word word, UpdateWordRequest request) {
        if (request.getWord() != null && !request.getWord().isEmpty()) {
            word.setCharacters(CharacterUtils.wordToJsonArray(request.getWord()));
        }
    }

    // Parse JSON string to List<String> for Response
    @AfterMapping
    default void parseCharactersAfterMapping(@MappingTarget WordResponse response, Word word) {
        if (word.getCharacters() != null && !word.getCharacters().isEmpty()) {
            response.setCharacters(CharacterUtils.jsonArrayToList(word.getCharacters()));
        }
    }
}
