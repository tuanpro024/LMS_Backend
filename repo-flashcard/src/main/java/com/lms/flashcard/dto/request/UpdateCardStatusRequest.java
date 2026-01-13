package com.lms.flashcard.dto.request;

import com.lms.flashcard.entity.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardStatusRequest {

    @NotNull(message = "Status is required")
    private CardStatus status;
}
