package com.lms.multimedia.repository;

import com.lms.multimedia.entity.Video;
import com.lms.multimedia.entity.enums.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByCode(String code);

    List<Video> findByStudySetId(String studySetId);

    boolean existsByCode(String code);

    // Soft delete support - exclude deleted videos
    List<Video> findByDeletedFalse();

    Optional<Video> findByIdAndDeletedFalse(String id);

    Optional<Video> findByCodeAndDeletedFalse(String code);

    List<Video> findByStatusAndDeletedFalse(VideoStatus status);

    List<Video> findByStudySetIdAndDeletedFalse(String studySetId);

    long countByStudySetIdAndDeletedFalse(String studySetId);
}
