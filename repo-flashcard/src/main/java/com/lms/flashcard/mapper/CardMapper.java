package com.lms.flashcard.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.flashcard.dto.request.CreateCardRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "studySet", ignore = true)
    @Mapping(target = "characters", source = "characters", qualifiedByName = "charactersToJson")
    Card toEntity(CreateCardRequest request);

    @Mapping(target = "characters", source = "characters", qualifiedByName = "jsonToCharacters")
    CardResponse toResponse(Card card);

    List<CardResponse> toResponseList(List<Card> cards);

    @Named("charactersToJson")
    default String charactersToJson(List<CreateCardRequest.CharacterInfo> characters) {
        if (characters == null || characters.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(characters);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToCharacters")
    default List<CardResponse.CharacterInfo> jsonToCharacters(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<CardResponse.CharacterInfo>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
