package com.lms.flashcard.mapper;

import com.lms.flashcard.dto.request.CreateFolderRequest;
import com.lms.flashcard.dto.response.FolderResponse;
import com.lms.flashcard.entity.Folder;
import com.lms.flashcard.entity.StudySet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FolderMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "parentFolder", ignore = true)
    @Mapping(target = "subfolders", ignore = true)
    @Mapping(target = "studySets", ignore = true)
    Folder toEntity(CreateFolderRequest request);

    @Mapping(target = "parentFolderId", expression = "java(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)")
    @Mapping(target = "studySetIds", expression = "java(mapStudySetIds(folder.getStudySets()))")
    FolderResponse toResponse(Folder folder);

    List<FolderResponse> toResponseList(List<Folder> folders);

    default List<String> mapStudySetIds(List<StudySet> studySets) {
        if (studySets == null)
            return List.of();
        return studySets.stream()
                .map(StudySet::getId)
                .collect(Collectors.toList());
    }
}
