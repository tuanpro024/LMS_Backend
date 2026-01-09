package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreateCardRequest;
import com.lms.flashcard.dto.response.CardResponse;
import com.lms.flashcard.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "studySet", ignore = true)
    Card toEntity(CreateCardRequest request);

    CardResponse toResponse(Card card);

    List<CardResponse> toResponseList(List<Card> cards);
}
