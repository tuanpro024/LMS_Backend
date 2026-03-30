package com.lms.content.common.mapper;

import com.lms.content.common.dto.request.CreateFolderRequest;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { StudySetMapper.class })
public interface FolderMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "studySets", ignore = true)
    Folder toEntity(CreateFolderRequest request);

    @Mapping(target = "studySets", ignore = true)
    @Mapping(target = "packageId", expression = "java(folder.getPackageEntity() != null ? folder.getPackageEntity().getId() : null)")
    FolderResponse toResponse(Folder folder);

    List<FolderResponse> toResponseList(List<Folder> folders);
}
