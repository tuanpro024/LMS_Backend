package com.lms.onllearning.mapper;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import com.lms.onllearning.entity.LeadRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    @Mapping(target = "courseCode", source = "code")
    @Mapping(target = "courseName", source = "name")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "status", ignore = true)       // set to PENDING in service
    @Mapping(target = "completedAt", ignore = true)
    LeadRegistration toEntity(LeadRegistrationRequest request);

    @Mapping(target = "code", source = "courseCode")
    @Mapping(target = "name", source = "courseName")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    LeadRegistrationResponse toResponse(LeadRegistration entity);
}
