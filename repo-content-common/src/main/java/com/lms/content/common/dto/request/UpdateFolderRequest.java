package com.lms.content.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFolderRequest {

    private String name;

    private String description;

    private String thumbnail;

    private Boolean isPrivate;
}
