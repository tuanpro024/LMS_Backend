package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreatePackageRequest;
import com.lms.flashcard.dto.response.PackageResponse;
import com.lms.flashcard.entity.Package;
import com.lms.flashcard.entity.TypeName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { SubjectMapper.class })
public interface PackageMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "type", ignore = true)
    Package toEntity(CreatePackageRequest request);

    @Mapping(target = "type", expression = "java(mapTypeToTypeName(packageEntity.getType()))")
    PackageResponse toResponse(Package packageEntity);

    List<PackageResponse> toResponseList(List<Package> packages);

    // Helper method to map Type entity to TypeName enum
    default TypeName mapTypeToTypeName(com.lms.flashcard.entity.Type type) {
        return type != null ? type.getName() : null;
    }
}
