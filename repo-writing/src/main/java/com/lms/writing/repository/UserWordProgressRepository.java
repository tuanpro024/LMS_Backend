package com.lms.writing.repository;

import com.lms.content.common.entity.enums.ContentStatus;
import com.lms.writing.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, String> {

        @Query("SELECT DISTINCT uwp.word.studySet.id FROM UserWordProgress uwp WHERE uwp.userId = :userId")
        Set<String> findDistinctStudySetIdsByUserId(@Param("userId") String userId);

        Optional<UserWordProgress> findByUserIdAndWordId(String userId, String wordId);

        @Query("SELECT uwp.word.id FROM UserWordProgress uwp WHERE uwp.userId = :userId AND uwp.word.studySet.id = :studySetId AND uwp.status = :status")
        List<String> findWordIdsByUserIdAndStudySetIdAndStatus(@Param("userId") String userId,
                        @Param("studySetId") String studySetId, @Param("status") ContentStatus status);

        @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.userId = :userId AND uwp.word.studySet.id = :studySetId AND uwp.status = :status")
        long countByUserIdAndStudySetIdAndStatus(@Param("userId") String userId, @Param("studySetId") String studySetId,
                        @Param("status") ContentStatus status);

        void deleteByWordId(String wordId);
}
