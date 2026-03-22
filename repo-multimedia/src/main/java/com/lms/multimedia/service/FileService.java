package com.lms.multimedia.service;

import org.springframework.web.multipart.MultipartFile;
import com.lms.multimedia.dto.FileDownload;
import com.lms.multimedia.dto.FileResponse;

public interface FileService {

    FileResponse upload(MultipartFile file);

    FileDownload download(String fileId);

    void commit(String fileId);

    void cleanupPending();
}
