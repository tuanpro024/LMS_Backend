package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;
import com.lms.flashcard.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { StudySetMapper.class })
public interface FolderMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "studySets", ignore = true)
    @Mapping(target = "slot", ignore = true)
    Folder toEntity(CreateFolderRequest request);

    @Mapping(target = "slotId", expression = "java(folder.getSlot() != null ? folder.getSlot().getId() : null)")
    FolderResponse toResponse(Folder folder);

    List<FolderResponse> toResponseList(List<Folder> folders);
}
