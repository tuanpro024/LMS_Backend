package com.lms.multimedia.dto.response;

import com.lms.multimedia.entity.enums.SubtitleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleResponse {

    private String id;
    private String videoId;
    private String videoCode;
    private String name;
    private String filePath;
    private SubtitleStatus status;
    private String createdAt;
    private String updatedAt;
}
