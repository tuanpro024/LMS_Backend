package com.lms.multimedia.repository;

import com.lms.content.common.entity.Video;
import com.lms.content.common.entity.enums.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByCode(String code);

    List<Video> findByUserId(String userId);

    List<Video> findByStatus(VideoStatus status);

    boolean existsByCode(String code);
}
