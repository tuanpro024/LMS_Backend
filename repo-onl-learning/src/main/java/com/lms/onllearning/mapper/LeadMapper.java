package com.lms.onllearning.mapper;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import com.lms.onllearning.entity.LeadRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    LeadRegistration toEntity(LeadRegistrationRequest request);

    LeadRegistrationResponse toResponse(LeadRegistration entity);
}
