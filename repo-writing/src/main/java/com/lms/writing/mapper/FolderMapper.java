package com.lms.writing.mapper;

import com.lms.writing.dto.request.CreateFolderRequest;
import com.lms.writing.dto.request.UpdateFolderRequest;
import com.lms.writing.dto.response.FolderResponse;
import com.lms.writing.entity.Folder;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { StudySetMapper.class })
public interface FolderMapper {

    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "slot", ignore = true)
    @Mapping(target = "studySets", ignore = true)
    Folder toEntity(CreateFolderRequest request);

    @Mapping(target = "packageId", source = "packageEntity.id")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "slotId", source = "slot.id")
    FolderResponse toResponse(Folder folder);

    List<FolderResponse> toResponseList(List<Folder> folders);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "packageEntity", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "slot", ignore = true)
    @Mapping(target = "studySets", ignore = true)
    void updateEntity(@MappingTarget Folder folder, UpdateFolderRequest request);
}
