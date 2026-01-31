package com.lms.content.common.mapper;

import com.lms.content.common.dto.request.CreateSlotRequest;
import com.lms.content.common.dto.response.SlotResponse;
import com.lms.content.common.entity.Slot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SlotMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Slot toEntity(CreateSlotRequest request);

    @Mapping(target = "subjectId", expression = "java(slot.getSubject() != null ? slot.getSubject().getId() : null)")
    SlotResponse toResponse(Slot slot);

    List<SlotResponse> toResponseList(List<Slot> slots);
}
