package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreateSlotRequest;
import com.lms.writing.dto.request.UpdateSlotRequest;
import com.lms.writing.dto.response.SlotResponse;
import com.lms.writing.entity.Slot;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { FolderMapper.class })
public interface SlotMapper {

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Slot toEntity(CreateSlotRequest request);

    @Mapping(target = "subjectId", source = "subject.id")
    SlotResponse toResponse(Slot slot);

    List<SlotResponse> toResponseList(List<Slot> slots);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "folders", ignore = true)
    void updateEntity(@MappingTarget Slot slot, UpdateSlotRequest request);
}
