package com.lms.videocourse.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.client.MultimediaClient;
import com.lms.videocourse.client.dto.MultimediaVideoResponse;
import com.lms.videocourse.dto.response.AvailableVideoResponse;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.service.IAvailableVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableVideoServiceImpl implements IAvailableVideoService {

    private final MultimediaClient multimediaClient;

    @Override
    public List<AvailableVideoResponse> getAllAvailableVideos(String query) {
        log.info("Fetching available videos from repo-multimedia, query={}", query);
        try {
            ApiResponse<List<MultimediaVideoResponse>> response = multimediaClient.getAllVideos(query);
            List<MultimediaVideoResponse> videos = response.data();
            if (videos == null)
                return new ArrayList<>();
            return videos.stream()
                    .map(this::toAvailableVideoResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching videos from repo-multimedia: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public AvailableVideoResponse getVideoByCode(String videoCode) {
        log.info("Fetching video by code={} from repo-multimedia", videoCode);
        try {
            MultimediaVideoResponse video = multimediaClient.getVideoByCode(videoCode);
            if (video == null) {
                throw new ResourceNotFoundException("Video not found with code: " + videoCode);
            }
            return toAvailableVideoResponse(video);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching video by code {}: {}", videoCode, e.getMessage());
            throw new ResourceNotFoundException("Video not found with code: " + videoCode);
        }
    }

    private AvailableVideoResponse toAvailableVideoResponse(MultimediaVideoResponse v) {
        return AvailableVideoResponse.builder()
                .videoCode(v.getCode())
                .title(v.getName())
                .description(v.getDescription())
                .videoUrl(v.getPlaylistUrl())
                .thumbnailUrl(v.getThumbnailPath())
                .duration(v.getDuration())
                .repoName("repo-multimedia")
                .build();
    }
}
