package com.lms.flashcard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudySetRequest {

    private String title;

    private String description;

    private Boolean isPrivate;

    private List<CreateCardRequest> cards;
}
