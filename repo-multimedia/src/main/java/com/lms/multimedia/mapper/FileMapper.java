package com.lms.multimedia.mapper;

import org.springframework.stereotype.Component;
import com.lms.multimedia.dto.FileResponse;
import com.lms.multimedia.entity.File;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileMapper {
    public FileResponse toResponse(File file) {
        if (file == null) return null;
        return new FileResponse(
                file.getId(),
                file.getFilename(),
                file.getMimeType(),
                file.getSizeBytes()
        );
    }
    
    public List<FileResponse> toResponseList(List<File> files) {
        if (files == null) return null;
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
