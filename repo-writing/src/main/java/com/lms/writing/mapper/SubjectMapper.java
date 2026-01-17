package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreateSubjectRequest;
import com.lms.writing.dto.request.UpdateSubjectRequest;
import com.lms.writing.dto.response.SubjectResponse;
import com.lms.writing.entity.Subject;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { SlotMapper.class,
        FolderMapper.class })
public interface SubjectMapper {

    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Subject toEntity(CreateSubjectRequest request);

    @Mapping(target = "packageId", source = "packageEntity.id")
    SubjectResponse toResponse(Subject subject);

    List<SubjectResponse> toResponseList(List<Subject> subjects);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "folders", ignore = true)
    void updateEntity(@MappingTarget Subject subject, UpdateSubjectRequest request);
}
