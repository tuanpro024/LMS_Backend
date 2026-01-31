package com.lms.writing.repository;

import com.lms.content.common.entity.enums.ContentStatus;
import com.lms.writing.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, String> {

    Optional<UserWordProgress> findByUserIdAndWordId(String userId, String wordId);

    @Query("SELECT uwp.word.id FROM UserWordProgress uwp WHERE uwp.userId = :userId AND uwp.word.studySet.id = :studySetId AND uwp.status = :status")
    List<String> findWordIdsByUserIdAndStudySetIdAndStatus(@Param("userId") String userId,
            @Param("studySetId") String studySetId, @Param("status") ContentStatus status);

    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.userId = :userId AND uwp.word.studySet.id = :studySetId AND uwp.status = :status")
    long countByUserIdAndStudySetIdAndStatus(@Param("userId") String userId, @Param("studySetId") String studySetId,
            @Param("status") ContentStatus status);
}
