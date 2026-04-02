package com.lms.kanjiorigin.dto.request;

import com.lms.kanjiorigin.entity.enums.KanjiStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKanjiStatusRequest {

    @NotNull(message = "Status is required")
    private KanjiStatus status;
}