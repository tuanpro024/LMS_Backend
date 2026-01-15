package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreateSlotRequest;
import com.lms.flashcard.dto.response.SlotResponse;
import com.lms.flashcard.entity.Slot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { FolderMapper.class })
public interface SlotMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Slot toEntity(CreateSlotRequest request);

    @Mapping(target = "subjectId", expression = "java(slot.getSubject() != null ? slot.getSubject().getId() : null)")
    SlotResponse toResponse(Slot slot);

    List<SlotResponse> toResponseList(List<Slot> slots);
}
