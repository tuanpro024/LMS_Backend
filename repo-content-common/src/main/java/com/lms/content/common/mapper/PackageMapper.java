package com.lms.content.common.mapper;

import com.lms.content.common.dto.request.CreatePackageRequest;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.TypeName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { FolderMapper.class })
public interface PackageMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "folders", ignore = true)
    @Mapping(target = "type", ignore = true)
    Package toEntity(CreatePackageRequest request);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "type", expression = "java(mapTypeToTypeName(packageEntity.getType()))")
    PackageResponse toResponse(Package packageEntity);

    List<PackageResponse> toResponseList(List<Package> packages);

    // Helper method to map Type entity to TypeName enum
    default TypeName mapTypeToTypeName(com.lms.content.common.entity.Type type) {
        return type != null ? type.getName() : null;
    }
}
