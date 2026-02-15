package com.lms.learningpath.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.entity.TypeName;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * Feign client for repo-pronunciation service.
 * Currently only handles Excel import operations.
 */
@FeignClient(name = "repo-pronunciation", contextId = "pronunciation", configuration = com.lms.learningpath.config.FeignConfig.class)
public interface PronunciationClient {

    /**
     * Import pronunciation content from Excel file
     */
    @PostMapping(value = "/packages/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<HierarchicalImportResult> importExcel(
            @RequestPart("file") MultipartFile file,
            @RequestParam("typeName") TypeName typeName);
}
