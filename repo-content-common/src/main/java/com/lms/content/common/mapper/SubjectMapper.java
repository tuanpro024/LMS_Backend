package com.lms.content.common.mapper;

import com.lms.content.common.dto.request.CreateSubjectRequest;
import com.lms.content.common.dto.response.SubjectResponse;
import com.lms.content.common.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { SlotMapper.class })
public interface SubjectMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Subject toEntity(CreateSubjectRequest request);

    @Mapping(target = "packageId", expression = "java(subject.getPackageEntity() != null ? subject.getPackageEntity().getId() : null)")
    SubjectResponse toResponse(Subject subject);

    List<SubjectResponse> toResponseList(List<Subject> subjects);
}
