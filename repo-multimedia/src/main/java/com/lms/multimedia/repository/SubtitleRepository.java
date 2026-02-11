package com.lms.multimedia.repository;

import com.lms.multimedia.entity.Subtitle;
import com.lms.multimedia.entity.enums.SubtitleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtitleRepository extends JpaRepository<Subtitle, String> {

    /**
     * Find all non-deleted subtitles for a video
     */
    List<Subtitle> findByVideoIdAndDeletedFalse(String videoId);

    /**
     * Find all subtitles for a video (including deleted)
     */
    List<Subtitle> findByVideoId(String videoId);

    /**
     * Find all non-deleted subtitles for a video by video code
     */
    List<Subtitle> findByVideoCodeAndDeletedFalse(String videoCode);

    /**
     * Find the active subtitle for a video
     */
    Optional<Subtitle> findByVideoIdAndStatusAndDeletedFalse(String videoId, SubtitleStatus status);

    /**
     * Find all subtitles created by a user
     */

    @Modifying
    @Query("UPDATE Subtitle s SET s.status = :status WHERE s.videoId = :videoId AND s.id != :excludeId AND s.deleted = false")
    void updateStatusByVideoIdExcept(@Param("videoId") String videoId,
            @Param("excludeId") String excludeId,
            @Param("status") SubtitleStatus status);

    /**
     * Count non-deleted subtitles for a video
     */
    long countByVideoIdAndDeletedFalse(String videoId);
}
