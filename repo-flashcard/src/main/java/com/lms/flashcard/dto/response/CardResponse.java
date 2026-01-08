package com.lms.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private String id;
    private String term;
    private String definition;
    private int cardIndex;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
