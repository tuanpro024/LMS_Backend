package com.lms.multimedia.dto;

import java.io.InputStream;

public record FileDownload(
        InputStream stream,
        String filename,
        String contentType,
        Long sizeBytes
) {
}
