package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreatePackageRequest;
import com.lms.writing.dto.request.UpdatePackageRequest;
import com.lms.writing.dto.response.PackageResponse;
import com.lms.writing.entity.Package;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { SubjectMapper.class,
        FolderMapper.class })
public interface PackageMapper {

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Package toEntity(CreatePackageRequest request);

    @Mapping(target = "type", source = "type.name")
    PackageResponse toResponse(Package packageEntity);

    List<PackageResponse> toResponseList(List<Package> packages);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "folders", ignore = true)
    void updateEntity(@MappingTarget Package packageEntity, UpdatePackageRequest request);
}
