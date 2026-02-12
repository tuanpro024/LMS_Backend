package com.lms.listening.repository;

import com.lms.listening.entity.VideoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoMetadataRepository extends JpaRepository<VideoMetadata, String> {

    /**
     * Find all videos in a study set, ordered by displayOrder
     */
    List<VideoMetadata> findByStudySetIdAndDeletedFalseOrderByDisplayOrder(String studySetId);

    /**
     * Find specific video in a study set
     */
    Optional<VideoMetadata> findByStudySetIdAndVideoCodeAndDeletedFalse(String studySetId, String videoCode);

    /**
     * Check if video already exists in study set
     */
    boolean existsByStudySetIdAndVideoCodeAndDeletedFalse(String studySetId, String videoCode);

    /**
     * Count videos in a study set
     */
    long countByStudySetIdAndDeletedFalse(String studySetId);

    /**
     * Find all videos by video code (across all study sets)
     */
    List<VideoMetadata> findByVideoCodeAndDeletedFalse(String videoCode);
}
