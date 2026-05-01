package com.lms.pronunciation.repository;

import com.lms.pronunciation.entity.PronunciationItemStudySetProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PronunciationItemStudySetProgressRepository extends JpaRepository<PronunciationItemStudySetProgress, String> {

    Optional<PronunciationItemStudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<PronunciationItemStudySetProgress> findByUserId(String userId);

    void deleteByStudySetId(String studySetId);
}
