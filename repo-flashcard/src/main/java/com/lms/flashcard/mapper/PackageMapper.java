package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreatePackageRequest;
import com.lms.flashcard.dto.response.PackageResponse;
import com.lms.flashcard.entity.Package;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { FolderMapper.class })
public interface PackageMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "folders", ignore = true)
    Package toEntity(CreatePackageRequest request);

    PackageResponse toResponse(Package packageEntity);

    List<PackageResponse> toResponseList(List<Package> packages);
}
