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
    @Mapping(target = "status", ignore = true)       // set to PENDING_SALES in service
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    LeadRegistration toEntity(LeadRegistrationRequest request);

    @Mapping(target = "code", source = "courseCode")
    @Mapping(target = "name", source = "courseName")
    LeadRegistrationResponse toResponse(LeadRegistration entity);
}
