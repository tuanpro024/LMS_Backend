package com.lms.multimedia.dto;

public record FileResponse(
        String fileId,
        String filename,
        String mimeType,
        Long sizeBytes
) {
}
